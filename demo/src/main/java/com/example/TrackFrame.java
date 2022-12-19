package com.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class TrackFrame extends JFrame {
        // 디스플레이 화면 설정
        public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        public int fontSize = (int) (((screenSize.getWidth() / 80) + (screenSize.getHeight() / 80)) * 1.2);
        public Font trackFont = new Font("Arial Rounded MT Bold", Font.BOLD, fontSize);
        public JScrollPane scrollPanel;
        public JPanel leftTrackPanel = new JPanel();
        public ReversePanel rightTrackPanel = new ReversePanel();
        public String leftText = "";
        public JTextArea leftTextArea = new JTextArea(), rightTextArea = new JTextArea();
        public String rightText = "";

        private static void drawString(Graphics2D g, String text, int lineWidth, int x, int y) {
            FontMetrics m = g.getFontMetrics();
            if (Math.abs(m.stringWidth(text)) < lineWidth) {
                for (String line : text.split("\n"))
                    g.drawString(line, x, y += g.getFontMetrics().getHeight());
            } else {
                String[] words = text.split("");
                String currentLine = words[0];
    
                for (int i = 1; i < words.length; i++) {
                    if (words[i].equals("\n")) {
                        g.drawString(currentLine, x, y);
                        y += m.getHeight();
                        currentLine = words[i];
                        continue;
                    }
                    if (Math.abs(m.stringWidth(currentLine + words[i])) < lineWidth) {
                        currentLine += words[i];
                    } else {
                        g.drawString(currentLine, x, y);
                        y += m.getHeight();
                        currentLine = words[i];
                    }
                }
    
                if(currentLine.trim().length() > 0) {
                    g.drawString(currentLine, x, y);
                }
            }
        }

        class ReversePanel extends JPanel {
            public String text = "";
            public Color color = Color.BLACK;
    
           @Override
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setFont(trackFont);
                g2.setColor(color);
                Font oldFont = g2.getFont();
                Font newFont = oldFont.deriveFont(AffineTransform.getScaleInstance(-1, 1));
                g2.setFont(newFont);
                drawString(g2, text, (int) (screenSize.getWidth() - fontSize) / 2, (int) (screenSize.getWidth() - fontSize) / 2, fontSize);
                scrollPanel.getVerticalScrollBar().setValue(scrollPanel.getVerticalScrollBar().getMaximum());
            }
    
            public void addText(String text) {
                this.text = text;
                this.repaint();
                scrollPanel.getVerticalScrollBar().setValue(scrollPanel.getVerticalScrollBar().getMaximum());
            }
            
            public void printText() {
                System.out.println(this.text);
            }

            public void setColor(Color color) {
                this.color = color;
                this.repaint();
            }
        }

        public void initialize() {
            JPanel background = new JPanel();
            background.setBackground(Color.BLACK);
            background.setSize(screenSize);
            background.setLayout(new GridLayout(1, 2));
        
            leftTrackPanel.setLayout(new BoxLayout(leftTrackPanel, BoxLayout.Y_AXIS));
            leftTrackPanel.setBackground(Color.BLACK);

            leftTextArea.setEditable(false);
            leftTextArea.setFont(trackFont);
            leftTextArea.setBackground(Color.BLACK);
            leftTextArea.setForeground(Color.WHITE);
            leftTextArea.setText(leftText);
            leftTextArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            leftTextArea.setLineWrap(true);
            leftTextArea.setWrapStyleWord(true);
            
            leftTrackPanel.add(leftTextArea);

            rightTrackPanel.setBackground(Color.BLACK);

            background.add(leftTrackPanel);
            background.add(rightTrackPanel);

            scrollPanel = new JScrollPane(background);
            scrollPanel.setViewportView(background);

            add(scrollPanel);

            // 제목표시줄 없애기
            setUndecorated(true);

            setTitle("DMTLABS Track Program - Track");
            setSize(screenSize);
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            setVisible(true);
            
            System.out.println("폰트 사이즈 : " + fontSize);
            System.out.println("화면 해상도 : " + screenSize.width + " x " + screenSize.height);
        }
}
