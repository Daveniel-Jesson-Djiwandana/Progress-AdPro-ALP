package ui.components;

import ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

    private Color normalBackgroundColor;
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private Color currentDisplayColor;
    private final int cornerRadius = 6;

    // Constructor for custom colored buttons
    public RoundedButton(String buttonText, Color initialColor) {
        super(buttonText);

        setupVisuals(initialColor);
        setupInteractions();
    }

    // Default constructor using theme primary color
    public RoundedButton(String buttonText) {
        this(buttonText, UITheme.TEXT_PRIMARY);
    }

    // Initialize styling and color states
    private void setupVisuals(Color baseColor) {
        updateButtonColors(baseColor);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(UITheme.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(160, 40));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    // Calculate hover and press shades based on HSB
    private void updateButtonColors(Color baseColor) {
        this.normalBackgroundColor = baseColor;

        float[] hsbValues = Color.RGBtoHSB(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                null);

        float hue = hsbValues[0];
        float saturation = hsbValues[1];
        float brightness = hsbValues[2];

        this.hoverBackgroundColor = Color.getHSBColor(hue, saturation, Math.min(1.0f, brightness + 0.10f));
        this.pressedBackgroundColor = Color.getHSBColor(hue, saturation, Math.max(0.0f, brightness - 0.10f));
        this.currentDisplayColor = baseColor;

        // Dynamic text color adjustment
        if (brightness > 0.8f && saturation < 0.2f) {
            setForeground(UITheme.BG_DARK);
        } else {
            setForeground(Color.WHITE);
        }
    }

    // Handle mouse hover and click animations
    private void setupInteractions() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                currentDisplayColor = hoverBackgroundColor;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                currentDisplayColor = normalBackgroundColor;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent event) {
                currentDisplayColor = pressedBackgroundColor;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                currentDisplayColor = hoverBackgroundColor;
                repaint();
            }
        });
    }

    // Render the rounded background
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2d.setColor(currentDisplayColor);
        graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        graphics2d.dispose();

        super.paintComponent(graphics);
    }
    public void setBaseColor(Color newColor) {
        updateButtonColors(newColor);
        repaint();
    }
}