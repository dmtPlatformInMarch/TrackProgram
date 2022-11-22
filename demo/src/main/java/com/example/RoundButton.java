package com.example;

import java.awt.*;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;

public class RoundButton extends JButton {

    private Color btnColor = new Color(0, 255, 128);

    public RoundButton() {
        super();
    }

    public RoundButton(String text) {
        super(text);
    }

    public RoundButton(Action action) {
        super(action);
    }

    public RoundButton(Icon icon) {
        super(icon);
    }

    public RoundButton(String text, Icon icon) {
        super(text, icon);
    }

    public RoundButton(String text, Color color) {
        super(text);
        this.btnColor = color;
    }

    protected void decorate() {
        //setFocusPainted(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        Graphics2D graphics = (Graphics2D) g;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isRollover()) {
            graphics.setColor(Color.CYAN);
        } else if (getModel().isArmed()) {
            graphics.setColor(getBackground().brighter());
        } else if (!getModel().isEnabled()) {
            graphics.setColor(getBackground().darker());
        } else {
            //graphics.setColor(getBackground());
            graphics.setColor(btnColor);
        }

        graphics.fillRoundRect(0, 0, width, height, 25, 25);

        this.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));

        FontMetrics fontMetrics = graphics.getFontMetrics();
        Rectangle stringBounds = fontMetrics.getStringBounds(this.getText(), graphics).getBounds();

        int textX = (width - stringBounds.width) / 2;
        int textY = (height - stringBounds.height) / 2 + fontMetrics.getAscent();

        graphics.setColor(Color.BLACK);
        graphics.setFont(getFont());
        graphics.drawString(getText(), textX, textY);
        graphics.dispose();

        super.paintComponent(g);
    }
}
