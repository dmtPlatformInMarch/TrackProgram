package com.example;

import java.awt.*;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

public class JframeExample extends JFrame {

    private final JPanel P1;
    private final JPanel P2;
    private final JPanel main;
    private final JScrollPane scrol;
    private final JButton jButton;
    private final JButton jButton2;

    private int listNumber;

    public JframeExample() {
        listNumber = 0;
        P1 = new JPanel();
        P2 = new JPanel();
        P2.setLayout(new BoxLayout(P2, BoxLayout.Y_AXIS));
        main = new JPanel();
        jButton = new JButton("Add");
        jButton2 = new JButton("Remove");
        scrol = new JScrollPane(P2);
        initialize();
        this.add(main);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setVisible(true);

    }

    public static void main(String[] args) {
        JframeExample jframeExample = new JframeExample();
    }

    private void addPressed(ActionEvent evt) {
        System.out.println("Add Pressed");
        JPanel newPanel = new JPanel();
        newPanel.setPreferredSize(new Dimension(165, 80));
        newPanel.setMaximumSize(new Dimension(165, 80));
        newPanel.setMinimumSize(new Dimension(165, 80));
        newPanel.setBorder(new LineBorder(Color.red, 2));
        newPanel.add(new JLabel(listNumber + " - Example Text"));
        P2.add(newPanel);
        listNumber++;
        revalidate();
    }

    private void removePressed(ActionEvent evt) {
        System.out.println("Remove Pressed");
        listNumber = 0;
        P2.removeAll();
        revalidate();
    }

    private void initialize() {
        main.setLayout(new GridLayout(1, 2));
        main.add(P1);
        main.add(scrol);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addPressed(evt);
            }
        });
        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removePressed(evt);
            }
        });
        P1.add(jButton);
        P1.add(jButton2);
    }

}