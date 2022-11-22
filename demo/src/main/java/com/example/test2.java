package com.example;

import javax.sound.sampled.DataLine.Info;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/*
 * 마이크 변경 로직 테스트
 */
public class test2 {
    private static ArrayList<String> mic = new ArrayList<String>();

    private static void findMicList() throws LineUnavailableException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] targetLineInfos = m.getTargetLineInfo();
            Line.Info[] target = m.getTargetLineInfo(new Info(
                TargetDataLine.class,
                new AudioFormat(16000, 16, 1, true, false)
            ));

            for (Line.Info line:target) {
                TargetDataLine newTarget = (TargetDataLine) AudioSystem.getLine(line);
                if (!AudioSystem.isLineSupported(line)) {
                    System.out.println("Microphone not supported");
                    System.exit(0);
                }
                System.out.println("오디오 타겟 라인 : " + newTarget.getLineInfo());
                System.out.println("오디오 타겟 포맷 : " + newTarget.getFormat());
                newTarget.close();
            }

            /*for (Line.Info lineInfo:targetLineInfos) {
                
                if (m.isLineSupported(lineInfo) && !info.getVersion().equals("Unknown Version")) {
                    //System.out.println("\n=========================================================================================\n오디오 믹서 : " + info.getName() + "\n-----------------------------------------------------------------------------------------\n");
                    System.out.println("라인 마이크 정보 : " + info);
                    //System.out.println(info.getName() + " \\ " + info.getVersion());
                    mic.add(info.getName().replace("Port ", ""));
                    //System.out.println("\n=========================================================================================");
                    
                }
            }*/
        }

        System.out.println("검색된 마이크 : " + mic.toString());
    }

    public static void main(String[] args) throws Exception {
        TargetDataLine targetDataLine;
        AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
        DataLine.Info targetInfo =
            new Info(
                TargetDataLine.class,
                audioFormat); // Set the system information to read from the microphone audio
        // stream

        if (!AudioSystem.isLineSupported(targetInfo)) {
            System.out.println("Microphone not supported");
            System.exit(0);
        }
        // Target data line captures the audio stream the microphone produces.
        targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        System.out.println("타켓 마이크 정보 : " + targetDataLine.getLineInfo());
        System.out.println("타켓 마이크 포맷 : " + targetDataLine.getFormat());

        findMicList();
    }
}
