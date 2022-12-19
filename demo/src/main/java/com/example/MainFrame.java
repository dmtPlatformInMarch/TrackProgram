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
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;

public class MainFrame extends JFrame {

    private JComboBox<String> outLangSelector;
    private JComboBox<String> inLangSelector;

    private TrackFrame track;
    //private Thread recordingVoice;
    private Thread mainUserVoice, subUserVoice;
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private InfiniteStreamRecognizeOptions option;
    //private GoogleSpeechStream googleSpeechStream;
    private GoogleSpeechStream mainUserSpeechStream, subUserSpeechStream;
    
    private ArrayList<String> mic = new ArrayList<String>();
    private ArrayList<Mixer> micMixer = new ArrayList<Mixer>();
    private AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
    private DataLine.Info targetLineInfo = new Info(TargetDataLine.class, audioFormat);
    private TargetDataLine mainMic;
    private TargetDataLine subMic;

    private String[] googleLanguageCode = {"ko-KR", "en-US", "zh", "ja-JP", "de-DE", "fr-FR", "es-ES", "pt-PT", "it-IT", "ru-RU", "vi-VN"};
    private String[] language = { "한국어", "영어", "중국어", "일본어", "독일어", "프랑스어" };
    private String[] inLanguage = language;
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
    private void findMicList () throws LineUnavailableException {
        mainMic = (TargetDataLine) AudioSystem.getLine(targetLineInfo);
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] targetLineInfos = m.getTargetLineInfo();

            for (Line.Info targetInfo:targetLineInfos) {
                if (m.isLineSupported(targetInfo) && targetInfo.matches(mainMic.getLineInfo())) {
                    System.out.println("\n=========================================================================================\n오디오 믹서 : " + info.getName() + "\n-----------------------------------------------------------------------------------------\n");
                    System.out.println(info.getName() + " \\ " + info.getVersion());
                    mic.add(m.getMixerInfo().getName().replace("Port ", ""));
                    micMixer.add(m);
                    System.out.println("\n=========================================================================================");
                }
            }
        }

        System.out.println("검색된 마이크 : " + mic.toString());
    }
    
    public void initialize() {
        try {
            findMicList();

            //googleSpeechStream = new GoogleSpeechStream(option.langCode, track);
            mainUserSpeechStream = new GoogleSpeechStream(option.langCode, track, "left");
            subUserSpeechStream = new GoogleSpeechStream(option.langCode, track, "right");

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

            JPanel mainMicGroup = new JPanel();
            JPanel subMicGroup = new JPanel();
            mainMicGroup.setLayout(new GridBagLayout());
            mainMicGroup.setBackground(Color.WHITE);
            subMicGroup.setLayout(new GridBagLayout());
            subMicGroup.setBackground(Color.WHITE);
            JLabel mainMicTitle = new JLabel("메인 마이크");
            JLabel subMicTitle = new JLabel("서브 마이크");
            mainMicTitle.setFont(mainFont);
            subMicTitle.setFont(mainFont);
            JComboBox<String> mainMicSelector = new JComboBox(mic.toArray());
            JComboBox<String> subMicSelector = new JComboBox(mic.toArray());

            // mainUser 마이크 선택 이벤트
            ActionListener mainMicSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String selectMic = cb.getSelectedItem().toString();
                    try {
                        System.out.println("검색한 마이크 믹서 : " + micMixer.get(mic.indexOf(selectMic)).isLineSupported(targetLineInfo));
                        mainMic = (TargetDataLine) (micMixer.get(mic.indexOf(cb.getSelectedItem().toString())).getLine(targetLineInfo));
                        
                    } catch (LineUnavailableException ex) {
                        System.out.println("해당 Line 사용 불가능 : " + cb.getSelectedItem().toString());
                        //ex.printStackTrace();
                    }
                }
            };

            // subUser 마이크 선택 이벤트
            ActionListener subMicSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String selectMic = cb.getSelectedItem().toString();
                    try {
                        subMic = (TargetDataLine) micMixer.get(mic.indexOf(selectMic)).getLine(targetLineInfo);
                    } catch (LineUnavailableException ex) {
                        System.out.println("해당 Line 사용 불가능 : " + cb.getSelectedItem().toString());
                        ex.printStackTrace();
                    }
                }
            };

            mainMicSelector.addActionListener(mainMicSelect);
            subMicSelector.addActionListener(subMicSelect);
            
            con.fill = GridBagConstraints.NONE;
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.3;
            mainMicGroup.add(mainMicTitle, con);
            con.weightx = 0.7;
            con.fill = GridBagConstraints.HORIZONTAL;
            mainMicGroup.add(mainMicSelector, con);
            con.weightx = 0.3;
            subMicGroup.add(subMicTitle, con);
            con.weightx = 0.7;
            con.fill = GridBagConstraints.HORIZONTAL;
            subMicGroup.add(subMicSelector, con);

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

            // 초기 언어 설정
            mainUserSpeechStream.setLanguageCode(googleLanguageCode[Arrays.asList(language).indexOf(inLangSelector.getSelectedItem().toString())]);
            mainUserSpeechStream.setInputLanguage(inLangSelector.getSelectedItem().toString());
            mainUserSpeechStream.setOutputLanguage(outLangSelector.getSelectedItem().toString());
            subUserSpeechStream.setLanguageCode(googleLanguageCode[Arrays.asList(language).indexOf(outLangSelector.getSelectedItem().toString())]);
            subUserSpeechStream.setInputLanguage(outLangSelector.getSelectedItem().toString());
            subUserSpeechStream.setOutputLanguage(inLangSelector.getSelectedItem().toString());

            // inLanguage 선택 이벤트
            ActionListener inLangSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();

                    // inLanguage를 선택했다면, 선택한 요소를 제외한 outLanguage를 만들어야 함.
                    List<String> outList = new ArrayList<>(Arrays.asList(language));
                    outList.remove(cb.getSelectedItem().toString());
                    outLanguage = outList.toArray(new String[0]);
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(outLanguage);
                    outLangSelector.setModel(model);

                    // 옵션의 언어 설정 변경
                    option.setLanguageCode(googleLanguageCode[cb.getSelectedIndex()]);

                    // 음성 인식기의 언어 설정 변경
                    mainUserSpeechStream.setLanguageCode(option.langCode);
                    //googleSpeechStream.setLanguageCode(option.langCode);

                    // 음성 인식기에서 번역 언어 설정 변경 (Input)
                    mainUserSpeechStream.setInputLanguage(cb.getSelectedItem().toString());
                    subUserSpeechStream.setOutputLanguage(cb.getSelectedItem().toString());
                    //googleSpeechStream.setInputLanguage(cb.getSelectedItem().toString());
                    //System.out.println("Select Input Lang : " + cb.getSelectedItem().toString());
                    
                    // 음성 인식기에서 번역 언어 설정 변경 (Output)
                    mainUserSpeechStream.setOutputLanguage(outLangSelector.getSelectedItem().toString());
                    subUserSpeechStream.setInputLanguage(outLangSelector.getSelectedItem().toString());

                    //googleSpeechStream.setOutputLanguage(outLangSelector.getSelectedItem().toString());
                    //System.out.println("Select Output Lang : " + outLangSelector.getSelectedItem().toString());
                }
            };

            // outLanguage 선택 이벤트
            ActionListener outLangSelect = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();

                    // outLanguage를 선택했다면, 선택한 요소를 제외한 inLanguage를 만들어야 함.
                    List<String> inList = new ArrayList<>(Arrays.asList(language));
                    inList.remove(cb.getSelectedItem().toString());
                    inLanguage = inList.toArray(new String[0]);
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(inLanguage);
                    inLangSelector.setModel(model);

                    // 옵션의 언어 설정 변경
                    option.setOutLanguageCode(googleLanguageCode[cb.getSelectedIndex()]);

                    // 음성 인식기의 언어 설정 변경
                    subUserSpeechStream.setLanguageCode(option.outlangCode);

                    // 음성 인식기에서 번역 언어 설정 변경 (Input)
                    subUserSpeechStream.setInputLanguage(cb.getSelectedItem().toString());
                    mainUserSpeechStream.setOutputLanguage(cb.getSelectedItem().toString());

                    // 음성 인식기에서 번역 언어 설정 변경 (Output)
                    subUserSpeechStream.setOutputLanguage(inLangSelector.getSelectedItem().toString());
                    mainUserSpeechStream.setInputLanguage(inLangSelector.getSelectedItem().toString());
                    
                    //googleSpeechStream.setOutputLanguage(cb.getSelectedItem().toString());
                    //System.out.println("Select Output Lang : " + outLangSelector.getSelectedItem().toString());
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

            // 마이크 오픈 이벤트
            ActionListener startRecording = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainUserSpeechStream.setMicSetting(mainMic, audioFormat);
                    subUserSpeechStream.setMicSetting(subMic, audioFormat);
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

                    mainUserVoice = new Thread(mainUserSpeechStream);
                    subUserVoice = new Thread(subUserSpeechStream);
                    //recordingVoice = new Thread(googleSpeechStream);
                    //recordingVoice.start();
                    mainUserVoice.start();
                    subUserVoice.start();
                }
            };

            // 마이크 클로즈 이벤트
            ActionListener stopRecording = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Recording Stop !!!");
                    mainUserVoice.interrupt();
                    subUserVoice.interrupt();
                    //recordingVoice.interrupt();

                    mainUserSpeechStream.cleanMic();
                    subUserSpeechStream.cleanMic();
                    //googleSpeechStream.cleanMic();

                    recordingStart.setEnabled(true);
                    recordingStop.setEnabled(false);
                }
            };

            recordingStart.addActionListener(startRecording);
            recordingStop.addActionListener(stopRecording);

            actionGroup.add(recordingStart);
            actionGroup.add(recordingStop);

            setting.add(mainMicGroup);
            setting.add(subMicGroup);
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
