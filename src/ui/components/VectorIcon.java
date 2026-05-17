package ui.components;

import ui.UITheme;
import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Path2D;

public class VectorIcon implements Icon {
    
    public enum Type {
        FIRE, REPORT, STATUS, HISTORY, LOGOUT, TRUCK, CHECK, 
        DICE, SETTINGS, PEOPLE, WRENCH, DROP, PLUS, ALERT, USER, BOLT, REFRESH, BACK
    }

    private final Type iconType;
    private final int iconSize;
    private final Color iconColor;

    public VectorIcon(Type type, int size, Color color) {
        this.iconType = type;
        this.iconSize = size;
        this.iconColor = color;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int xOffset, int yOffset) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        
        // Setup rendering quality and translation
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.translate(xOffset, yOffset);
        
        // Scale from native 24x24 coordinate system to requested size
        double scaleFactor = iconSize / 24.0;
        graphics2d.scale(scaleFactor, scaleFactor);
        
        // Setup line styling
        graphics2d.setColor(iconColor);
        graphics2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw the specific vector shape
        switch (iconType) {
            case FIRE:
                drawFireIcon(graphics2d);
                break;
            case REPORT:
                graphics2d.drawRoundRect(5, 3, 14, 18, 2, 2);
                graphics2d.drawLine(8, 7, 16, 7);
                graphics2d.drawLine(8, 11, 16, 11);
                graphics2d.drawLine(8, 15, 12, 15);
                break;
            case STATUS:
                graphics2d.drawOval(4, 4, 16, 16);
                graphics2d.drawOval(8, 8, 8, 8);
                graphics2d.fillOval(11, 11, 2, 2);
                break;
            case HISTORY:
                graphics2d.drawOval(3, 3, 18, 18);
                graphics2d.drawLine(12, 7, 12, 12);
                graphics2d.drawLine(12, 12, 16, 12);
                break;
            case LOGOUT:
                graphics2d.drawLine(12, 4, 12, 14);
                graphics2d.drawLine(8, 10, 12, 14);
                graphics2d.drawLine(16, 10, 12, 14);
                graphics2d.drawArc(6, 6, 12, 12, -45, -270);
                break;
            case TRUCK:
                graphics2d.drawRect(2, 10, 14, 8);
                graphics2d.drawRect(16, 12, 6, 6);
                graphics2d.drawOval(4, 18, 4, 4);
                graphics2d.drawOval(16, 18, 4, 4);
                graphics2d.drawLine(2, 6, 16, 6);
                graphics2d.drawLine(2, 8, 16, 8);
                break;
            case CHECK:
                graphics2d.drawLine(4, 12, 10, 18);
                graphics2d.drawLine(10, 18, 20, 6);
                break;
            case DICE:
                graphics2d.drawRoundRect(3, 3, 18, 18, 4, 4);
                graphics2d.fillOval(7, 7, 3, 3);
                graphics2d.fillOval(14, 14, 3, 3);
                graphics2d.fillOval(10, 10, 4, 4);
                break;
            case SETTINGS:
                graphics2d.drawOval(7, 7, 10, 10);
                for(int i = 0; i < 8; i++) {
                    graphics2d.drawLine(12, 2, 12, 5);
                    graphics2d.rotate(Math.PI / 4, 12, 12);
                }
                break;
            case PEOPLE:
                graphics2d.drawOval(7, 4, 4, 4);
                graphics2d.drawOval(13, 4, 4, 4);
                graphics2d.drawLine(9, 8, 9, 14);
                graphics2d.drawLine(15, 8, 15, 14);
                graphics2d.drawLine(7, 14, 7, 20);
                graphics2d.drawLine(11, 14, 11, 20);
                graphics2d.drawLine(13, 14, 13, 20);
                graphics2d.drawLine(17, 14, 17, 20);
                break;
            case WRENCH:
                graphics2d.drawOval(14, 4, 6, 6);
                graphics2d.drawLine(17, 10, 7, 20);
                graphics2d.drawLine(14, 7, 5, 16);
                break;
            case DROP:
                Path2D dropShape = new Path2D.Double();
                dropShape.moveTo(12, 20);
                dropShape.curveTo(6, 20, 6, 14, 12, 4);
                dropShape.curveTo(18, 14, 18, 20, 12, 20);
                graphics2d.fill(dropShape);
                break;
            case PLUS:
                graphics2d.drawLine(12, 4, 12, 20);
                graphics2d.drawLine(4, 12, 20, 12);
                break;
            case ALERT:
                Path2D alertShape = new Path2D.Double();
                alertShape.moveTo(12, 2);
                alertShape.lineTo(2, 20);
                alertShape.lineTo(22, 20);
                alertShape.closePath();
                graphics2d.draw(alertShape);
                graphics2d.drawLine(12, 8, 12, 15);
                graphics2d.fillOval(11, 17, 2, 2);
                break;
            case USER:
                graphics2d.drawOval(8, 4, 8, 8);
                Path2D userBody = new Path2D.Double();
                userBody.moveTo(4, 20);
                userBody.curveTo(4, 14, 20, 14, 20, 20);
                graphics2d.draw(userBody);
                break;
            case BOLT:
                Path2D boltShape = new Path2D.Double();
                boltShape.moveTo(14, 2);
                boltShape.lineTo(6, 12);
                boltShape.lineTo(12, 12);
                boltShape.lineTo(10, 22);
                boltShape.lineTo(18, 10);
                boltShape.lineTo(12, 10);
                boltShape.closePath();
                graphics2d.fill(boltShape);
                break;
            case REFRESH:
                graphics2d.drawArc(4, 4, 16, 16, 45, 270);
                graphics2d.drawLine(20, 12, 20, 16);
                graphics2d.drawLine(20, 12, 16, 12);
                break;
            case BACK:
                graphics2d.drawLine(20, 12, 4, 12);
                graphics2d.drawLine(4, 12, 10, 6);
                graphics2d.drawLine(4, 12, 10, 18);
                break;
        }
        
        graphics2d.dispose();
    }

    private void drawFireIcon(Graphics2D g2) {
        Path2D outerFlame = new Path2D.Double();
        outerFlame.moveTo(12, 22);
        outerFlame.curveTo(6, 22, 4, 16, 6, 12);
        outerFlame.curveTo(7, 10, 10, 8, 12, 2);
        outerFlame.curveTo(14, 8, 17, 10, 18, 12);
        outerFlame.curveTo(20, 16, 18, 22, 12, 22);
        g2.fill(outerFlame);

        // Subtracted inner flame using background color
        g2.setColor(UITheme.BG_DARK);
        Path2D innerFlame = new Path2D.Double();
        innerFlame.moveTo(12, 18);
        innerFlame.curveTo(9, 18, 8, 15, 9, 13);
        innerFlame.curveTo(10, 12, 11, 11, 12, 8);
        innerFlame.curveTo(13, 11, 14, 12, 15, 13);
        innerFlame.curveTo(16, 15, 15, 18, 12, 18);
        g2.fill(innerFlame);
    }

    @Override
    public int getIconWidth() { return iconSize; }

    @Override
    public int getIconHeight() { return iconSize; }
}