package com.example;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;

public class TrackBoard extends JFrame {

    public GridBagConstraints constraints;
    public GridBagLayout gridBag;

    public Dimension screenSize = new Dimension(1920, 1080);
    //public Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public int fontSize = (int) (((screenSize.getWidth() / 80) + (screenSize.getHeight() / 80)) * 1.2);
    public Font trackFont = new Font("Helvetica", Font.BOLD, fontSize);
    public int listNumber = 0;

    public String leftText = "";
    public String rightText = "";

    public JPanel background;
    public JScrollPane scrollPanel;
    public JPanel controlPanel;
    public JPanel main;
    public JPanel leftTrackPanel;
    public JPanel rightTrackPanel;
    public JButton addButton;
    public JButton removeButton;

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

            leftPanel = new JPanel();
            rightPanel = new JPanel();

            leftTextArea = new JTextArea();
            rightTextArea = new JTextArea();

            leftTextArea.setLineWrap(true);
            rightTextArea.setLineWrap(true);

            add(leftPanel);
            add(rightPanel);
        }

        public LinePanel(String left, String right) {
            setLayout(new GridLayout(1,2));

            leftPanel = new JPanel();
            rightPanel = new JPanel();

            leftTextArea = new JTextArea();
            rightTextArea = new JTextArea();

            leftPanel.setLayout(new BorderLayout());
            rightPanel.setLayout(new BorderLayout());
            
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

            leftPanel.add(leftTextArea);
            rightPanel.add(rightTextArea);

            add(leftPanel);
            add(rightPanel);
        }

        public void setLeftText(String str) {
            leftText = str;
        }

        public void setRightText(String str) { 
            rightText = str;
        }
    }

    public TrackBoard() {
        constraints = new GridBagConstraints();
        gridBag = new GridBagLayout();
        controlPanel = new JPanel();
        main = new JPanel();
        main.setLayout(gridBag);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        background = new JPanel();
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        scrollPanel = new JScrollPane(main);
        //scrollPanel.setPreferredSize();
        scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        initialize();
        this.add(background);

        // 제목표시줄 없애기
        //setUndecorated(true);

        setSize(screenSize);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        System.out.println("폰트 사이즈 : " + fontSize);
        System.out.println("화면 해상도 : " + screenSize.width + " x " + screenSize.height);
    }

    public void addList(GridBagLayout layout, GridBagConstraints con, Component obj) {
        System.out.println("Add " + listNumber + " th Component!!");
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

    public void addPressed(ActionEvent e) {
        addList(gridBag, constraints, new LinePanel("안녕", "반가워\n오른쪽이 더 길게\n된다면?"));
        scrollPanel.getVerticalScrollBar().setValue(scrollPanel.getVerticalScrollBar().getMaximum());
    }

    public void removePressed(ActionEvent e) {
        System.out.println("Remove Pressed!");
        main.removeAll();
        revalidate();
    }
    
    public void initialize() {
        GridBagLayout backgroundLayout = new GridBagLayout();
        GridBagConstraints backgroundConstraints = new GridBagConstraints();

        background.setLayout(backgroundLayout);
        background.setBorder(new LineBorder(Color.RED, 2));
        backgroundConstraints.fill = GridBagConstraints.BOTH;
        
        scrollPanel.setPreferredSize(new Dimension((int) screenSize.getWidth(), (int) (screenSize.getHeight() * 0.75)));
        backgroundConstraints.gridx = 0;
        backgroundConstraints.gridy = 0;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 0.75;
        backgroundLayout.setConstraints(scrollPanel, backgroundConstraints);
        background.add(scrollPanel);

        controlPanel.setPreferredSize(new Dimension((int) screenSize.getWidth(), (int) (screenSize.getHeight() * 0.25)));
        backgroundConstraints.gridx = 0;
        backgroundConstraints.gridy = 1;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 0.25;
        backgroundLayout.setConstraints(controlPanel, backgroundConstraints);
        background.add(controlPanel);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed(e);
            }
        });

        controlPanel.add(addButton);
        controlPanel.add(removeButton);
    }

    public static void main(String[] args) {
        TrackBoard tr = new TrackBoard();
    }
}
