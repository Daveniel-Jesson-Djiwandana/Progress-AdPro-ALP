package ui.components;

import ui.UITheme;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private final Color backgroundColor;
    private final int cornerRadius;
    private boolean shouldShowBorder;

    
    public RoundedPanel(Color backgroundColor, int cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.cornerRadius = cornerRadius;
        setOpaque(false);
    }

    
    public RoundedPanel(Color backgroundColor) {
        this(backgroundColor, 12);
    }

    
    public RoundedPanel() {
        this(UITheme.BG_CARD, 12);
    }

    
    public void setHasBorder(boolean showBorder) {
        this.shouldShowBorder = showBorder;
        repaint();
    }

    
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        graphics2d.setColor(backgroundColor);
        graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        
        if (shouldShowBorder) {
            graphics2d.setColor(UITheme.BORDER);
            graphics2d.setStroke(new BasicStroke(1.0f));
            graphics2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        graphics2d.dispose();
        super.paintComponent(graphics);
    }
}