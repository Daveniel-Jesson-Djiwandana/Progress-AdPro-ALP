package ui.components;

import model.*;
import ui.UITheme;

import javax.swing.*;
import java.awt.*;

public class StatusBadge extends JLabel {

    public StatusBadge(String text, Color bg) {
        super(text, SwingConstants.CENTER);
        setFont(UITheme.FONT_SMALL);
        setForeground(Color.WHITE);
        setOpaque(false);
        setPreferredSize(new Dimension(110, 22));
        putClientProperty("bg", bg);
    }

    public static StatusBadge forStatus(IncidentStatus status) {
        Color color;
        switch (status) {
            case DISPATCHED:
                color = UITheme.STATUS_DISPATCHED;
                break;
            case RESOLVED:
                color = UITheme.STATUS_RESOLVED;
                break;
            default:
                color = UITheme.STATUS_REPORTED;
                break;
        }
        return new StatusBadge(status.getLabel(), color);
    }

    public static StatusBadge forSeverity(IncidentSeverity sev) {
        Color color;
        if (sev == null) {
            return new StatusBadge("N/A", UITheme.INFO);
        }
        switch (sev) {
            case RED:
                color = UITheme.WARNING; // Vibrant Amber Orange for Red
                break;
            case DOUBLE_RED:
                color = UITheme.ACCENT; // Vibrant Emergency Red for Double Red
                break;
            case TRIPLE_RED:
                color = new Color(255, 59, 48); // High contrast Apple Red for Triple Red
                break;
            case UNDETERMINED:
            default:
                color = new Color(100, 116, 139); // Slate-500 for Undetermined
                break;
        }
        return new StatusBadge(sev.getLabel(), color);
    }

    public static StatusBadge forTruckStatus(TruckStatus ts) {
        Color color;
        switch (ts) {
            case DEPLOYED:
                color = UITheme.STATUS_DISPATCHED;
                break;
            case MAINTENANCE:
                color = UITheme.WARNING;
                break;
            default:
                color = UITheme.SUCCESS;
                break;
        }
        return new StatusBadge(ts.getLabel(), color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color bg = (Color) getClientProperty("bg");
        if (bg == null)
            bg = UITheme.BG_CARD;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
