package com.example;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;

import net.sf.json.JSONObject;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

public class TwowayStream implements Runnable {
  private static final int STREAMING_LIMIT = 290000; // ~5 minutes

  public static final String RED = "\033[0;31m";
  public static final String GREEN = "\033[0;32m";
  public static final String YELLOW = "\033[0;33m";

  public Boolean isMicSupported = false;

  // Creating shared object
  private volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue();
  private TargetDataLine targetDataLine;
  private AudioFormat audioFormat;
  private DataLine.Info targetInfo;

  private int BYTES_PER_BUFFER = 6400; // buffer size in bytes

  private int restartCounter = 0;
  private ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
  private ArrayList<ByteString> lastAudioInput = new ArrayList<ByteString>();
  private int resultEndTimeInMS = 0;
  private int isFinalEndTime = 0;
  private int finalRequestEndTime = 0;
  private boolean newStream = true;
  private double bridgingOffset = 0;
  private boolean lastTranscriptWasFinal = false;
  private StreamController referenceToStreamController;
  private ByteString tempByteString;

  private String direction = "none"; // none, left, right;
  private String languageCode;
  private String[] dmtLanguageCode = {"ko", "en", "zh-CN", "ja", "de", "fr", "es", "pt", "it", "ru", "vi"};
  private String dmtFromLang = dmtLanguageCode[0];
  private String dmtToLang = dmtLanguageCode[1];

  private TwowayTrackFrame trackView;

  private Thread micThread;

  public TwowayStream(String languageCode, TwowayTrackFrame trackView) {
    this.languageCode = languageCode;
    this.trackView = trackView;
    this.direction = "none";
  }

  public TwowayStream(String languageCode, TwowayTrackFrame trackView, String dir) {
    this.languageCode = languageCode;
    this.trackView = trackView;
    this.direction = dir;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public void setMicSetting(TargetDataLine line, AudioFormat format) {
    this.targetDataLine = line;
    this.audioFormat = format;
    this.targetInfo = new Info(TargetDataLine.class, audioFormat);
  }

  public void setInputLanguage(String langCode) {
    switch(langCode) {
      case "한국어":
        dmtFromLang = dmtLanguageCode[0];
        break;
      case "영어":
        dmtFromLang = dmtLanguageCode[1];
        break;
      case "중국어":
        dmtFromLang = dmtLanguageCode[2];
        break;
      case "일본어":
        dmtFromLang = dmtLanguageCode[3];
        break;
      case "독일어":
        dmtFromLang = dmtLanguageCode[4];
        break;
      case "프랑스어":
        dmtFromLang = dmtLanguageCode[5];
        break;
      default:
        break;
    }
  }

  public void setOutputLanguage(String langCode) {
    switch(langCode) {
      case "한국어":
        dmtToLang = dmtLanguageCode[0];
        break;
      case "영어":
        dmtToLang = dmtLanguageCode[1];
        break;
      case "중국어":
        dmtToLang = dmtLanguageCode[2];
        break;
      case "일본어":
        dmtToLang = dmtLanguageCode[3];
        break;
      case "독일어":
        dmtToLang = dmtLanguageCode[4];
        break;
      case "프랑스어":
        dmtToLang = dmtLanguageCode[5];
        break;
      default:
        break;
    }
  }

  public static String convertMillisToDate(double milliSeconds) {
    long millis = (long) milliSeconds;
    DecimalFormat format = new DecimalFormat();
    format.setMinimumIntegerDigits(2);
    return String.format(
        "%s:%s /",
        format.format(TimeUnit.MILLISECONDS.toMinutes(millis)),
        format.format(
            TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
  }

  // GoogleSpeechStream 스레드 실행
  // 스레드 내에서 무한 스트리밍 인식 함수 실행
  @Override
  public void run() {
    try {
      infiniteStreamingRecognize(languageCode, trackView);
    } catch (Exception e) {
      System.out.println("Exception caught: " + e);
      e.printStackTrace();
      return;
    }
  }

  // 중단 시 마이크 입력 중단
  public void cleanMic() {
    targetDataLine.stop();
    targetDataLine.close();
    micThread.interrupt();
  }

  // 무한 스트리밍 인식 함수
  /** Performs infinite streaming speech recognition */
  public void infiniteStreamingRecognize(String languageCode, final TwowayTrackFrame trackView) throws Exception {
    System.out.println("설정된 언어 코드 : " + languageCode);
    // 마이크 입력을 sharedQueue에 쌓음.
    // Microphone Input buffering
    class MicBuffer implements Runnable {
      @Override
      public void run() {
        System.out.println(YELLOW);
        System.out.println("Start speaking...Press Ctrl-C to stop");
        targetDataLine.start();
        byte[] data = new byte[BYTES_PER_BUFFER];
        while (targetDataLine.isOpen()) {
          try {
            int numBytesRead = targetDataLine.read(data, 0, data.length);
            if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
              continue;
            }
            sharedQueue.put(data.clone());
          } catch (InterruptedException e) {
            System.out.println("Microphone input buffering interrupted : " + e.getMessage());
          }
        }
      }
    }

    // GoogleSpeechStream 내부의 마이크 입력 버퍼 스레드
    // 마이크 인풋 스레드 생성
    MicBuffer micrunnable = new MicBuffer();
    micThread = new Thread(micrunnable);

    // 구글 API 요청 응답 옵저버 생성
    ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
    try (SpeechClient client = SpeechClient.create()) {
      ClientStream<StreamingRecognizeRequest> clientStream;
      responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
        ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

        public void onStart(StreamController controller) {
            referenceToStreamController = controller;
        }

        public void onResponse(StreamingRecognizeResponse response) {
          responses.add(response);
          StreamingRecognitionResult result = response.getResultsList().get(0);
          Duration resultEndTime = result.getResultEndTime();
          resultEndTimeInMS = (int) ((resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000));
          double correctedTime = resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

          SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);

          // 결과 출력
          if (result.getIsFinal()) {
            System.out.print(GREEN);
            // 지우기
            System.out.print("\033[2K\r");
            System.out.printf("%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());

            JSONObject req = new JSONObject();
            req.put("from", dmtFromLang);
            req.put("to", dmtToLang);
            req.put("text", alternative.getTranscript());
            try { // 입력 종료 시 출력
              // 기본 or 왼쪽 페이지 설정
              if (direction == "none" || direction == "left") {
                trackView.leftTextArea.setForeground(Color.WHITE);
                trackView.rightTrackPanel.setColor(Color.WHITE);
                trackView.leftText = trackView.leftText + alternative.getTranscript() + "\n\n";
                trackView.rightText = trackView.rightText + httpsTranslator.translateRequest(req) + "\n";
                trackView.leftTextArea.setText(trackView.leftText);
                trackView.rightTrackPanel.addText(trackView.rightText);
              // 오른쪽 페이지 설정
              } else {
                trackView.leftTextArea.setForeground(Color.WHITE);
                trackView.rightTrackPanel.setColor(Color.WHITE);
                trackView.rightText = trackView.rightText + alternative.getTranscript() + "\n\n";
                trackView.leftText = trackView.leftText + httpsTranslator.translateRequest(req) + "\n";
                trackView.leftTextArea.setText(trackView.leftText);
                trackView.rightTrackPanel.addText(trackView.rightText);
              }
              
              //trackView.scrollPanel.getVerticalScrollBar().setValue(trackView.scrollPanel.getVerticalScrollBar().getMaximum());
            } catch (Exception e) {
              e.printStackTrace();
            }

            isFinalEndTime = resultEndTimeInMS;
            lastTranscriptWasFinal = true;
          } else { // 진행상황 출력
            System.out.print(RED);
            // 지우기
            System.out.print("\033[2K\r");
            System.out.printf("%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
            // 기본 or 왼쪽 페이지 설정
            if (direction == "none" || direction == "left") {
              trackView.leftTextArea.setForeground(Color.BLUE);
              trackView.leftTextArea.setText(trackView.leftText + alternative.getTranscript());
            // 오른쪽 페이지 설정
            } else {
              trackView.rightTrackPanel.setColor(Color.BLUE);
              trackView.rightTrackPanel.addText(trackView.rightText + alternative.getTranscript());
            }

            lastTranscriptWasFinal = false;
          }
        }

        public void onComplete() {}

        public void onError(Throwable t) {}
      };

      // 옵저버를 통한 요청 전달
      clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

      RecognitionConfig recognitionConfig =
          RecognitionConfig.newBuilder()
              .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
              .setLanguageCode(languageCode)
              .setSampleRateHertz(44100)
              .build();

      StreamingRecognitionConfig streamingRecognitionConfig =
          StreamingRecognitionConfig.newBuilder()
              .setConfig(recognitionConfig)
              .setInterimResults(true)
              .build();

      StreamingRecognizeRequest request =
          StreamingRecognizeRequest.newBuilder()
              .setStreamingConfig(streamingRecognitionConfig)
              .build(); // The first request in a streaming call has to be a config

      clientStream.send(request);

      try {
        // 마이크 정의
        // 샘플링 레이트를 조절하면 인식 속도가 달라짐.
        
        // SampleRate : 44100Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
        // bigEndian: false
        if (this.audioFormat == null) {
          System.out.println("오디오 포맷 미설정... 자동 설정...");
          audioFormat = new AudioFormat(44100, 16, 1, true, false);
          targetInfo = new Info(TargetDataLine.class, audioFormat); // stream
        }

        if (!AudioSystem.isLineSupported(targetInfo)) {
          System.out.println("Microphone not supported");
          System.exit(0);
        }
        // Target data line captures the audio stream the microphone produces.
        if (this.targetDataLine == null) {
          System.out.println("마이크 미설정... 자동 설정...");
          targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        }
        targetDataLine.open(audioFormat);
        micThread.start();

        long startTime = System.currentTimeMillis();

        while (true) {

          long estimatedTime = System.currentTimeMillis() - startTime;

          if (estimatedTime >= STREAMING_LIMIT) {

            clientStream.closeSend();
            referenceToStreamController.cancel(); // remove Observer

            if (resultEndTimeInMS > 0) {
              finalRequestEndTime = isFinalEndTime;
            }
            resultEndTimeInMS = 0;

            lastAudioInput = null;
            lastAudioInput = audioInput;
            audioInput = new ArrayList<ByteString>();

            restartCounter++;

            if (!lastTranscriptWasFinal) {
              System.out.print('\n');
            }

            newStream = true;

            clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

            request =
                StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build();

            /*System.out.println(YELLOW);
            System.out.printf("%d: RESTARTING REQUEST\n", restartCounter * STREAMING_LIMIT);*/

            startTime = System.currentTimeMillis();

          } else {

            if ((newStream) && (lastAudioInput.size() > 0)) {
              // if this is the first audio from a new request
              // calculate amount of unfinalized audio from last request
              // resend the audio to the speech client before incoming audio
              double chunkTime = STREAMING_LIMIT / lastAudioInput.size();
              // ms length of each chunk in previous request audio arrayList
              if (chunkTime != 0) {
                if (bridgingOffset < 0) {
                  // bridging Offset accounts for time of resent audio
                  // calculated from last request
                  bridgingOffset = 0;
                }
                if (bridgingOffset > finalRequestEndTime) {
                  bridgingOffset = finalRequestEndTime;
                }
                int chunksFromMs =
                    (int) Math.floor((finalRequestEndTime - bridgingOffset) / chunkTime);
                // chunks from MS is number of chunks to resend
                bridgingOffset =
                    (int) Math.floor((lastAudioInput.size() - chunksFromMs) * chunkTime);
                // set bridging offset for next request
                for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
                  request =
                      StreamingRecognizeRequest.newBuilder()
                          .setAudioContent(lastAudioInput.get(i))
                          .build();
                  clientStream.send(request);
                }
              }
              newStream = false;
            }

            tempByteString = ByteString.copyFrom(sharedQueue.take());
            //System.out.println(tempByteString);

            request =
                StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

            audioInput.add(tempByteString);
          }

          clientStream.send(request);
        }
      } catch (Exception e) {
        System.out.println("음성 인식 파트 에러 : " + e);
        return;
      }
    }
  }
}
