package com.example;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class test2{

      public static JFrame frame;
      public static JPanel panel;
      public static GridBagLayout layoutManager;
      public static GridBagConstraints constraints;
      public static int listNumber = 0;

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

      private class drawPanel extends JPanel {
            
      }

      public static void addGrid(GridBagLayout layout, GridBagConstraints con, Component obj, int order) {
            System.out.println("Add " + order + " th Component!!");
            con.gridx = 0;
            con.gridy = order;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.weightx = 1;
            con.weighty = 0;
            layout.setConstraints(obj, con);
            panel.add(obj);
            listNumber++;
            frame.revalidate();
      }
      public static void main(String[] args) {
            frame = new JFrame();
            frame.setLayout(new GridLayout(1, 2));
            panel = new JPanel();
            JPanel control = new JPanel();
            JButton addButton = new JButton("추가");

            addButton.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                        addGrid(layoutManager, constraints, new JTextArea("추가 에리어"), listNumber);
                  }
            });

            control.add(addButton);
      
            layoutManager = new GridBagLayout();
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            panel.setLayout(layoutManager);

            JTextArea content1 = new JTextArea("텍스트 에리어");
            JTextArea content2 = new JTextArea("텍스트 에리어");
            JTextArea content3 = new JTextArea("텍스트 에리어");

            addGrid(layoutManager, constraints, content1, listNumber);
            addGrid(layoutManager, constraints, content2, listNumber);
            addGrid(layoutManager, constraints, content3, listNumber);

            frame.add(panel);
            frame.add(control);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(1920, 1080);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      }
}

