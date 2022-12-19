package com.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;

// 믹서에서 타겟라인을 타고 들어가야함.
// Mixer의 getLine을 통해 해당 포맷에 해당하는 마이크를 가져옴
// 포맷은 마이크들이 동일한 경우에도 믹서가 다르다는 것으로 구분.

public class test {

    public static String presentMic;
    public static JLabel micLabel;
    public static Boolean stopped = false;
    
    public static AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
    public static DataLine.Info targetLineInfo = new Info(TargetDataLine.class, audioFormat);
    public static TargetDataLine presentLine;
    public static ArrayList<String> micList = new ArrayList<String>();
    public static ArrayList<Mixer> MixerList = new ArrayList<Mixer>();

    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static class MicrophoneRecorder implements Runnable {
        AudioFormat format;
        TargetDataLine line;
        AudioInputStream audioInputStream;
    
        public MicrophoneRecorder() {
            super();
        }
    
        public MicrophoneRecorder(AudioFormat format, TargetDataLine line) {
            super();
            this.format = format;
            this.line = line;
        }
    
        @Override
        public void run() {
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int numBytesRead;
    
            while(!stopped) {
                if ((numBytesRead = line.read(data, 0, data.length)) == -1) break;
                out.write(data, 0, numBytesRead);
            }
    
            line.stop();
            line.close();
    
            try {
                data = new byte[bufferLengthInBytes];
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    
            final byte audioBytes[] = out.toByteArray();
            final ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
    
            File file = new File("record.wav");
    
            try {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
                audioInputStream.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            stopped = false;
        }
    }

    public static void micSearch() throws LineUnavailableException {
        presentLine = (TargetDataLine) AudioSystem.getLine(targetLineInfo);
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.println(Arrays.toString(mixerInfos) + "\n");

        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] targetLineInfos = m.getTargetLineInfo();

            for (Line.Info targetInfo:targetLineInfos) {
                if (m.isLineSupported(targetInfo) && targetInfo.matches(presentLine.getLineInfo())) {
                    micList.add(m.getMixerInfo().getName().replace("Port ", ""));
                    MixerList.add(m);
                }
            }
        }

        System.out.println("검색된 마이크 : " + micList.toString());
    }

    public static class BaseFrame extends JFrame {

        public BaseFrame() {
            super();
        }

        public void initialize() throws LineUnavailableException {
            
            JPanel all_panel = new JPanel();
            all_panel.setLayout(new GridLayout(3, 1));
            all_panel.setBounds(0, 0, 640, 320);

            presentMic = micList.get(0);
            micLabel = new JLabel(presentMic);

            all_panel.add(micLabel);

            JComboBox micListComboBox = new JComboBox(micList.toArray());

            ActionListener micSelectEvent = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    presentMic = cb.getSelectedItem().toString();
                    micLabel.setText(presentMic);
                    try {
                        presentLine = (TargetDataLine) MixerList.get(micList.indexOf(presentMic)).getLine(targetLineInfo);
                    } catch (LineUnavailableException ex) {
                        System.out.println("Line 가용성/설정 오류");
                        ex.printStackTrace();
                    }
                }
            };

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 2));
            buttonPanel.setBounds(0, 295, 640, 160);

            final JButton startMic = new JButton("마이크 시작");
            final JButton stopMic = new JButton("마이크 종료");


            ActionListener micOpen = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        presentLine.open(audioFormat);
                        presentLine.start();

                        Runnable record = new MicrophoneRecorder(audioFormat, presentLine);
                        Thread startRecording = new Thread(record);
                        startRecording.start();
                        startMic.setEnabled(false);
                        stopMic.setEnabled(true);
                    } catch (LineUnavailableException ex) {
                        System.out.println("Line 가용성/설정 오류");
                        ex.printStackTrace();
                    }
                }
            };

            ActionListener micClose = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopped = true;
                    startMic.setEnabled(true);
                        stopMic.setEnabled(false);
                }
            };

            startMic.addActionListener(micOpen);
            stopMic.addActionListener(micClose);

            buttonPanel.add(startMic);
            buttonPanel.add(stopMic);

            add(buttonPanel);

            micListComboBox.addActionListener(micSelectEvent);

            all_panel.add(micListComboBox);

            add(all_panel);

            setTitle("마이크 변경 예제");
            setSize( 640, 480 );
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    public static void main(String[] args) throws LineUnavailableException {
        micSearch();
        
        System.out.println();

        BaseFrame mainFrame = new BaseFrame();
        mainFrame.initialize();
    }
}
