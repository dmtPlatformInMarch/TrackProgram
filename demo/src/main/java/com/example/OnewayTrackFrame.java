package com.example;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.*;

public class OnewayTrackFrame extends JFrame {

    public GridBagConstraints constraints;
    public GridBagLayout gridBag;

    //public Dimension screenSize = new Dimension(1920, 1080);
    public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public int fontSize = (int) (((screenSize.getWidth() / 80) + (screenSize.getHeight() / 80)) * 1.2);
    public Font trackFont = new Font("Helvetica", Font.BOLD, fontSize);
    public int listNumber = 0;

    public JPanel main, controlPanel;
    public RealTimePanel leftTrackPanel, rightTrackPanel;
    public JScrollPane lineScrollPanel, leftScrollPanel, rightScrollPanel;

    // 실시간 자막 그리기 함수
    private void drawString(JPanel panel, Graphics2D g, String text, int lineWidth, int x, int y) {
        FontMetrics m = g.getFontMetrics();
        if (Math.abs(m.stringWidth(text)) < lineWidth) {
            for (String line : text.split("\n")) {
                g.drawString(line, x, y += g.getFontMetrics().getHeight());
            }   
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

        panel.setPreferredSize(new Dimension((int) screenSize.getWidth() / 2, y + fontSize / 2));
        panel.revalidate();
        panel.repaint();
    }

    // 실시간 자막 패널
    public class RealTimePanel extends JPanel {
        public JScrollPane scrollPanel;
        public String text;
        public Color color;

        public RealTimePanel(JScrollPane scrollPane) {
            super();
            this.text = "";
            this.color = Color.BLACK;
            this.scrollPanel = scrollPane;
        }

        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setFont(trackFont);
            g2.setColor(color);
            drawString(this, g2, text, (int) (screenSize.getWidth() - fontSize) / 2, 0, fontSize);
            this.scrollPanel.getVerticalScrollBar().setValue(this.scrollPanel.getVerticalScrollBar().getMaximum());
        }

        public void setText(String text) {
            this.text = text;
            this.revalidate();
        }

        public String getText() {
            return this.text;
        }

        public void addText(String text) {
            this.text += text;
            this.revalidate();
        }
        
        public void printText() {
            System.out.println(this.text);
        }

        public void setColor(Color color) {
            this.color = color;
            this.revalidate();
        }

        public void clearText() {
            this.text = "";
            this.revalidate();
        }
    }

    // 라인 패널 - 발화가 끝나는 시점의 완료 패널
    public class LinePanel extends JPanel {
        public JPanel leftPanel;
        public JPanel rightPanel;
        public JTextArea leftTextArea;
        public JTextArea rightTextArea;
        public String leftText;
        public String rightText;

        public int maxSize(int a, int b) {
            return (a > b) ? a : b;
        }

        public LinePanel() {
            setLayout(new GridLayout(1,2));

            leftTextArea = new JTextArea();
            rightTextArea = new JTextArea();

            leftTextArea.setBackground(Color.BLACK);
            leftTextArea.setForeground(Color.WHITE);
            leftTextArea.setEditable(false);
            leftTextArea.setFont(trackFont);
            leftTextArea.setLineWrap(true);
            rightTextArea.setBackground(Color.BLACK);
            rightTextArea.setForeground(Color.WHITE);
            rightTextArea.setEditable(false);
            rightTextArea.setFont(trackFont);
            rightTextArea.setLineWrap(true);

            add(leftTextArea);
            add(rightTextArea);
        }

        public LinePanel(String left, String right) {
            setLayout(new GridLayout(1,2));

            leftTextArea = new JTextArea();
            rightTextArea = new JTextArea();
            
            leftTextArea.setBackground(Color.BLACK);
            leftTextArea.setForeground(Color.WHITE);
            leftTextArea.setEditable(false);
            leftTextArea.setFont(trackFont);
            leftTextArea.setLineWrap(true);
            rightTextArea.setBackground(Color.BLACK);
            rightTextArea.setForeground(Color.WHITE);
            rightTextArea.setEditable(false);
            rightTextArea.setFont(trackFont);
            rightTextArea.setLineWrap(true);

            setLeftText(left);
            setRightText(right);

            leftTextArea.setText(this.leftText);
            rightTextArea.setText(this.rightText);

            add(leftTextArea);
            add(rightTextArea);
        }

        public void setLeftText(String str) {
            leftText = str;
        }

        public void setRightText(String str) { 
            rightText = str;
        }

        public void setLeftColor(Color color) {
            leftTextArea.setForeground(color);
        }

        public void setRightColor(Color color) {
            rightTextArea.setForeground(color);
        }
    }
    // 라인 패널 메소드
    public void addList(GridBagLayout layout, GridBagConstraints con, Component obj) {
        con.gridx = 0;
        con.gridy = listNumber;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.weightx = 1;
        con.weighty = 0;
        layout.setConstraints(obj, con);
        main.add(obj);
        listNumber++;
        revalidate();
    }
    // Non-Color Option - 구분 없는 텍스트
    public void addLine(String left, String right) {
        addList(gridBag, constraints, new LinePanel(left, right));
        lineScrollPanel.getVerticalScrollBar().setValue(lineScrollPanel.getVerticalScrollBar().getMaximum());
    }
    // Color Option - '발화' 와 '번역' 을 구분
    public void addLine(String left, String right, Color leftColor, Color rightColor) {
        LinePanel newPanel = new LinePanel(left, right);
        newPanel.setLeftColor(leftColor);
        newPanel.setRightColor(rightColor);
        addList(gridBag, constraints, newPanel);
        lineScrollPanel.getVerticalScrollBar().setValue(lineScrollPanel.getVerticalScrollBar().getMaximum());
    }
    // LinePanel 초기화
    public void removePressed() {
        main.removeAll();
        lineScrollPanel.removeAll();
        revalidate();
    }

    class ResizeListener implements ComponentListener {
        JScrollPane scroll;

        public ResizeListener(JScrollPane scrollPane) {
            scroll = scrollPane;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void componentShown(ComponentEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            // TODO Auto-generated method stub
            
        }
    }

    // Constructor
    public OnewayTrackFrame() {
        lineScrollPanel = new JScrollPane();
        leftScrollPanel = new JScrollPane();
        rightScrollPanel = new JScrollPane();
        
        lineScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rightScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        gridBag = new GridBagLayout();

        main = new JPanel();
        main.addComponentListener(new ResizeListener(lineScrollPanel));
        main.setBackground(Color.BLACK);
        main.setLayout(gridBag);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 2));

        leftTrackPanel = new RealTimePanel(leftScrollPanel);
        rightTrackPanel = new RealTimePanel(rightScrollPanel);

        lineScrollPanel.setViewportView(main);
        leftScrollPanel.setViewportView(leftTrackPanel);
        rightScrollPanel.setViewportView(rightTrackPanel);

        controlPanel.add(leftScrollPanel);
        controlPanel.add(rightScrollPanel);
        
        initialize();

        // 제목표시줄 없애기
        setUndecorated(true);

        setSize(screenSize);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        System.out.println("폰트 사이즈 : " + fontSize);
        System.out.println("화면 해상도 : " + screenSize.width + " x " + screenSize.height);
    }
    
    public void initialize() {
        GridBagLayout backgroundLayout = new GridBagLayout();
        GridBagConstraints backgroundConstraints = new GridBagConstraints();

        setLayout(backgroundLayout);
        backgroundConstraints.fill = GridBagConstraints.BOTH;
        
        lineScrollPanel.setPreferredSize(new Dimension((int) screenSize.getWidth(), (int) (screenSize.getHeight() * 0.75)));
        backgroundConstraints.gridx = 0;
        backgroundConstraints.gridy = 0;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 0.75;
        backgroundLayout.setConstraints(lineScrollPanel, backgroundConstraints);
        add(lineScrollPanel);

        controlPanel.setPreferredSize(new Dimension((int) screenSize.getWidth(), (int) (screenSize.getHeight() * 0.25)));
        backgroundConstraints.gridx = 0;
        backgroundConstraints.gridy = 1;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 0.25;
        backgroundLayout.setConstraints(controlPanel, backgroundConstraints);
        add(controlPanel);
    }
}
