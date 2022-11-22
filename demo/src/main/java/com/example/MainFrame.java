package com.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    JComboBox<String> outLangSelector;
    JComboBox<String> inLangSelector;

    TrackFrame track;
    Thread recordingVoice;
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    InfiniteStreamRecognizeOptions option;
    GoogleSpeechStream googleSpeechStream;
    
    private String[] googleLanguageCode = {"ko-KR", "en-US", "zh", "ja-JP", "de-DE", "fr-FR", "es-ES", "pt-PT", "it-IT", "ru-RU", "vi-VN"};
    final private String[] language = { "한국어", "영어", "중국어", "일본어", "독일어", "프랑스어" };
    final private String[] inLanguage = language;
    private String[] outLanguage = {};

    public MainFrame () {
        super();
    }

    public MainFrame (TrackFrame trackFrame, InfiniteStreamRecognizeOptions options) {
        track = trackFrame;
        option = options;
    }

    final private Font mainFont = new Font("Arial Rounded MT Bold", Font.BOLD, 18);

    // 로고 그리기 함수
    final private JPanel logo = new JPanel() {
        Image logo = new ImageIcon(MainFrame.class.getResource("./source/logo.png")).getImage();

        public void paint(Graphics g) {
            int x = (this.getWidth() - logo.getWidth(null)) / 2;
            int y = (this.getHeight() - logo.getHeight(null)) / 2;
            g.drawImage(logo, x, y, null);
        }
    };

    // 마이크 검색 함수
    final private ArrayList<String> findMicList () {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        ArrayList<String> mic = new ArrayList<String>();

        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] targetLineInfos = m.getTargetLineInfo();

            for (Line.Info lineInfo:targetLineInfos) {
                if (m.isLineSupported(lineInfo) && !info.getVersion().equals("Unknown Version")) {
                    System.out.println("\n=========================================================================================\n오디오 믹서 : " + info.getName() + "\n-----------------------------------------------------------------------------------------\n");
                    System.out.println(info.getName() + " \\ " + info.getVersion());
                    mic.add(info.getName().replace("Port ", ""));
                    System.out.println("\n=========================================================================================");
                }
            }
        }

        System.out.println("검색된 마이크 : " + mic.toString());
        return mic;
    }
    
    public void initialize() {
        try {
            googleSpeechStream = new GoogleSpeechStream(option.langCode, track);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(2, 1));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(75, 75, 0, 75));
            mainPanel.setBackground(Color.WHITE);

            mainPanel.add(logo);

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(25 , 25 , 25 , 25)); 
            controlPanel.setSize(600, 400);
            controlPanel.setBackground(Color.WHITE);

            JPanel setting = new JPanel();
            GridBagConstraints con = new GridBagConstraints();
            setting.setLayout(new BoxLayout(setting, BoxLayout.Y_AXIS));
            setting.setSize(600, 300);
            setting.setBackground(Color.WHITE);

            JPanel micGroup = new JPanel();
            micGroup.setLayout(new GridBagLayout());
            micGroup.setBackground(Color.WHITE);
            JLabel micTitle = new JLabel("마이크");
            micTitle.setFont(mainFont);
            JComboBox<String> micSelector = new JComboBox(findMicList().toArray());
            
            con.fill = GridBagConstraints.NONE;
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.3;
            micGroup.add(micTitle, con);
            con.weightx = 0.7;
            con.fill = GridBagConstraints.HORIZONTAL;
            micGroup.add(micSelector, con);

            JPanel inLangGroup = new JPanel();
            inLangGroup.setLayout(new GridBagLayout());
            inLangGroup.setBackground(Color.WHITE);
            JLabel inLang = new JLabel("입력 언어");
            inLang.setFont(mainFont);
            inLangSelector = new JComboBox(inLanguage);

            con.fill = GridBagConstraints.NONE;
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.2;
            inLangGroup.add(inLang, con);
            con.weightx = 0.8;
            con.fill = GridBagConstraints.HORIZONTAL;
            inLangGroup.add(inLangSelector, con);

            JPanel outLangGroup = new JPanel();
            outLangGroup.setLayout(new GridBagLayout());
            outLangGroup.setBackground(Color.WHITE);
            JLabel outLang = new JLabel("출력 언어");
            outLang.setFont(mainFont);
            List<String> outList = new ArrayList<>(Arrays.asList(language));
            outList.remove(inLangSelector.getSelectedItem().toString());
            outLanguage = outList.toArray(new String[0]);
            outLangSelector = new JComboBox<String>(outLanguage);

            ActionListener inLangSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    List<String> outList = new ArrayList<>(Arrays.asList(language));
                    outList.remove(cb.getSelectedItem().toString());
                    outLanguage = outList.toArray(new String[0]);
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(outLanguage);
                    outLangSelector.setModel(model);
                    option.setLanguageCode(googleLanguageCode[cb.getSelectedIndex()]);
                    googleSpeechStream = new GoogleSpeechStream(option.langCode, track);
                    googleSpeechStream.setInputLanguage(cb.getSelectedItem().toString());
                    System.out.println("Select Input Lang : " + cb.getSelectedItem().toString());
                    
                    googleSpeechStream.setOutputLanguage(outLangSelector.getSelectedItem().toString());
                    System.out.println("Select Output Lang : " + outLangSelector.getSelectedItem().toString());
                }
            };

            ActionListener outLangSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    googleSpeechStream.setOutputLanguage(cb.getSelectedItem().toString());
                    System.out.println("Select Output Lang : " + outLangSelector.getSelectedItem().toString());
                }
            };

            inLangSelector.addActionListener(inLangSelect);
            outLangSelector.addActionListener(outLangSelect);

            con.fill = GridBagConstraints.NONE;
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.2;
            outLangGroup.add(outLang, con);
            con.weightx = 0.8;
            con.fill = GridBagConstraints.HORIZONTAL;
            outLangGroup.add(outLangSelector, con);

            JPanel actionGroup = new JPanel();
            actionGroup.setLayout(new FlowLayout(FlowLayout.CENTER));
            actionGroup.setBackground(Color.WHITE);

            final RoundButton recordingStart = new RoundButton("녹음하기");
            recordingStart.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
            final RoundButton recordingStop = new RoundButton("중단하기", Color.orange);
            recordingStop.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
            recordingStop.setEnabled(false);

            ActionListener startRecording = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        outStream.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    recordingStart.setEnabled(false);
                    recordingStop.setEnabled(true);

                    System.out.println("Recording Start !!!");
                    
                    if (option == null) {
                        // Could not parse.
                        System.out.println("Failed to parse options.");
                        System.exit(1);
                    }

                    recordingVoice = new Thread(googleSpeechStream);
                    recordingVoice.start();
                }
            };

            ActionListener stopRecording = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Recording Stop !!!");
                    recordingVoice.interrupt();
                    googleSpeechStream.cleanMic();

                    recordingStart.setEnabled(true);
                    recordingStop.setEnabled(false);
                }
            };

            recordingStart.addActionListener(startRecording);
            recordingStop.addActionListener(stopRecording);

            actionGroup.add(recordingStart);
            actionGroup.add(recordingStop);

            setting.add(micGroup);
            setting.add(inLangGroup);
            setting.add(outLangGroup);

            controlPanel.add(setting);
            controlPanel.add(actionGroup);

            mainPanel.add(controlPanel);

            add(mainPanel);

            setTitle("DMTLABS Track Program - Control");
            setSize(800, 800);
            setResizable(false);
            setMinimumSize(new Dimension(800, 800));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        } catch (Exception e) {
            System.out.println("에러 : " + e);
        }
    }
}
