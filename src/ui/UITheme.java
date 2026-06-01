package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class UITheme {

    // ── Palet: Hitam + Aksen Merah Tua #8c1b1b ──────────────────────────────
    public static final Color ACCENT        = new Color(0x8c, 0x1b, 0x1b); // #8c1b1b

    // Background
    public static final Color BG_DARK       = Color.BLACK;
    public static final Color BG_SURFACE    = new Color(10, 10, 10);
    public static final Color BG_CARD       = new Color(18, 18, 18);
    public static final Color BG_SIDEBAR    = Color.BLACK;

    // Backward-compat aliases — sekarang semua pakai ACCENT
    public static final Color ACCENT_RED    = ACCENT;
    public static final Color ACCENT_ORANGE = ACCENT;
    public static final Color ACCENT_YELLOW = ACCENT;

    // Typography
    public static final Color TEXT_PRIMARY   = new Color(240, 240, 240);
    public static final Color TEXT_SECONDARY = new Color(150, 150, 150);
    public static final Color TEXT_MUTED     = new Color(70, 70, 70);

    // Status — semua pakai aksen atau turunan hitam/putih
    public static final Color SUCCESS = new Color(180, 180, 180);  // abu terang (netral ok)
    public static final Color WARNING = ACCENT;                    // pakai aksen merah tua
    public static final Color DANGER  = ACCENT;                    // pakai aksen merah tua
    public static final Color INFO    = new Color(120, 120, 120);  // abu sedang

    // Borders
    public static final Color BORDER = new Color(45, 45, 45);

    // Incident status
    public static final Color STATUS_REPORTED   = INFO;
    public static final Color STATUS_DISPATCHED = ACCENT;
    public static final Color STATUS_RESOLVED   = SUCCESS;

    // Severities
    public static final Color SEV_LOW      = SUCCESS;
    public static final Color SEV_MEDIUM   = new Color(180, 180, 180);
    public static final Color SEV_HIGH     = ACCENT;
    public static final Color SEV_CRITICAL = ACCENT;

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";

    public static final Font FONT_TITLE   = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUB     = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BUTTON  = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 13);

    public static void styleTableHeader(JTable table, Font font) {
        table.getTableHeader().setFont(font);
        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(ACCENT);
        
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(BG_CARD);
                setOpaque(true);
                setForeground(ACCENT);
                setFont(font);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
                return this;
            }
        });
    }

    public static void apply() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Custom flat button painters to remove Nimbus gradients
        javax.swing.Painter<JButton> buttonPainter = new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setColor(object.getBackground());
                    g.fillRect(0, 0, width, height);
                }
            }
        };
        javax.swing.UIManager.put("Button[Enabled].backgroundPainter", buttonPainter);
        javax.swing.UIManager.put("Button[Focused].backgroundPainter", buttonPainter);
        javax.swing.UIManager.put("Button[Default].backgroundPainter", buttonPainter);
        javax.swing.UIManager.put("Button[Default+Focused].backgroundPainter", buttonPainter);
        
        javax.swing.UIManager.put("Button[MouseOver].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.min(255, bg.getRed() + 15), Math.min(255, bg.getGreen() + 15), Math.min(255, bg.getBlue() + 15)));
                    g.fillRect(0, 0, width, height);
                }
            }
        });
        javax.swing.UIManager.put("Button[Default+MouseOver].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.min(255, bg.getRed() + 15), Math.min(255, bg.getGreen() + 15), Math.min(255, bg.getBlue() + 15)));
                    g.fillRect(0, 0, width, height);
                }
            }
        });
        
        javax.swing.UIManager.put("Button[Pressed].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.max(0, bg.getRed() - 15), Math.max(0, bg.getGreen() - 15), Math.max(0, bg.getBlue() - 15)));
                    g.fillRect(0, 0, width, height);
                }
            }
        });
        javax.swing.UIManager.put("Button[Default+Pressed].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.max(0, bg.getRed() - 15), Math.max(0, bg.getGreen() - 15), Math.max(0, bg.getBlue() - 15)));
                    g.fillRect(0, 0, width, height);
                }
            }
        });

        javax.swing.Painter<JButton> disabledButtonPainter = new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setColor(new Color(30, 30, 30));
                    g.fillRect(0, 0, width, height);
                }
            }
        };
        javax.swing.UIManager.put("Button[Disabled].backgroundPainter", disabledButtonPainter);

        // Custom flat toggle button painters to remove Nimbus gradients
        javax.swing.Painter<JToggleButton> togglePainter = new javax.swing.Painter<JToggleButton>() {
            @Override
            public void paint(Graphics2D g, JToggleButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setColor(object.isSelected() ? ACCENT : object.getBackground());
                    g.fillRect(0, 0, width, height);
                }
            }
        };
        javax.swing.UIManager.put("ToggleButton[Enabled].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[Focused].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[MouseOver].backgroundPainter", new javax.swing.Painter<JToggleButton>() {
            @Override
            public void paint(Graphics2D g, JToggleButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.min(255, bg.getRed() + 15), Math.min(255, bg.getGreen() + 15), Math.min(255, bg.getBlue() + 15)));
                    g.fillRect(0, 0, width, height);
                }
            }
        });
        javax.swing.UIManager.put("ToggleButton[Pressed].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[Selected].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[Selected+MouseOver].backgroundPainter", togglePainter);
        
        javax.swing.Painter<JToggleButton> disabledTogglePainter = new javax.swing.Painter<JToggleButton>() {
            @Override
            public void paint(Graphics2D g, JToggleButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setColor(new Color(30, 30, 30));
                    g.fillRect(0, 0, width, height);
                }
            }
        };
        javax.swing.UIManager.put("ToggleButton[Disabled].backgroundPainter", disabledTogglePainter);
        javax.swing.UIManager.put("ToggleButton[Disabled+Selected].backgroundPainter", disabledTogglePainter);

        javax.swing.UIManager.put("control",                  BG_SURFACE);
        javax.swing.UIManager.put("info",                     BG_CARD);
        javax.swing.UIManager.put("nimbusBase",               BG_DARK);
        javax.swing.UIManager.put("nimbusBlueGrey",           BG_CARD);
        javax.swing.UIManager.put("nimbusLightBackground",    BG_CARD);
        javax.swing.UIManager.put("nimbusFocus",              new Color(0,0,0,0));
        javax.swing.UIManager.put("nimbusSelectionBackground",ACCENT);
        javax.swing.UIManager.put("text",                     TEXT_PRIMARY);

        javax.swing.UIManager.put("Table.background",         BG_SURFACE);
        javax.swing.UIManager.put("Table.foreground",         TEXT_PRIMARY);
        javax.swing.UIManager.put("Table.gridColor",          BORDER);
        javax.swing.UIManager.put("TableHeader.background",   BG_CARD);
        javax.swing.UIManager.put("TableHeader.foreground",   TEXT_SECONDARY);
        javax.swing.UIManager.put("TableHeader.font",         FONT_SMALL);

        javax.swing.UIManager.put("OptionPane.background",    BG_SURFACE);
        javax.swing.UIManager.put("Panel.background",         BG_SURFACE);

        javax.swing.UIManager.put("TextField.background",     BG_DARK);
        javax.swing.UIManager.put("TextField.foreground",     TEXT_PRIMARY);
        javax.swing.UIManager.put("TextField.caretForeground",TEXT_PRIMARY);

        javax.swing.UIManager.put("ScrollBar.thumb",          BORDER);
        javax.swing.UIManager.put("ScrollBar.track",          BG_DARK);
    }
}
