package com.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class test {

    public static String presentMic = "없음";

    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static class BaseFrame extends JFrame {

        public BaseFrame() {
            super();
        }

        public void initialize() {
            JPanel all_panel = new JPanel();
            
            JLabel micLabel = new JLabel("현재 마이크");
            JTextPane micPane = new JTextPane();
            micPane.setEditable(false);
            micPane.setText(presentMic);
            StyledDocument doc = micPane.getStyledDocument();
            SimpleAttributeSet cetner = new SimpleAttributeSet();
            StyleConstants.setAlignment(cetner, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), cetner, false);

            all_panel.add(micLabel);
            all_panel.add(micPane);

            JButton micButton1 = new JButton("마이크1");
            JButton micButton2 = new JButton("마이크2");

            setTitle("마이크 변경 예제");
            setSize( (int) screenSize.getWidth() / 2, (int) screenSize.getHeight() / 2 );
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        BaseFrame mainFrame = new BaseFrame();
        mainFrame.initialize();
    }
}
