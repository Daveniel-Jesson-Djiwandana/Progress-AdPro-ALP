package ui.components;

import model.*;
import ui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * A small pill-shaped label that shows a coloured status or severity badge.
 */
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
            case DISPATCHED: color = UITheme.STATUS_DISPATCHED; break;
            case RESOLVED:   color = UITheme.STATUS_RESOLVED;   break;
            default:         color = UITheme.STATUS_REPORTED;   break;
        }
        return new StatusBadge(status.getLabel(), color);
    }

    public static StatusBadge forSeverity(IncidentSeverity sev) {
        Color color;
        switch (sev) {
            case MEDIUM:   color = UITheme.SEV_MEDIUM;   break;
            case HIGH:     color = UITheme.SEV_HIGH;     break;
            case CRITICAL: color = UITheme.SEV_CRITICAL; break;
            default:       color = UITheme.SEV_LOW;      break;
        }
        return new StatusBadge(sev.getLabel(), color);
    }

    public static StatusBadge forTruckStatus(TruckStatus ts) {
        Color color;
        switch (ts) {
            case DEPLOYED:     color = UITheme.STATUS_DISPATCHED; break;
            case MAINTENANCE:  color = UITheme.WARNING;           break;
            default:           color = UITheme.SUCCESS;           break;
        }
        return new StatusBadge(ts.getLabel(), color);
    }

    public static StatusBadge forCivilianCondition(CivilianCondition cond) {
        Color color;
        switch (cond) {
            case INJURED:   color = UITheme.WARNING; break;
            case CRITICAL:  color = UITheme.DANGER;  break;
            case EVACUATED: color = UITheme.INFO;    break;
            default:        color = UITheme.SUCCESS; break;
        }
        return new StatusBadge(cond.getLabel(), color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color bg = (Color) getClientProperty("bg");
        if (bg == null) bg = UITheme.BG_CARD;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
        g2.dispose();
        super.paintComponent(g);
    }
}
