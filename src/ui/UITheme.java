package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class UITheme {

    // ── Palet: Slate + High-Contrast Emergency ──────────────────────────────
    public static final Color ACCENT = new Color(239, 68, 68); // #EF4444 Emergency Red
    public static final Color ACCENT_RED = new Color(239, 68, 68); // #EF4444
    public static final Color ACCENT_ORANGE = new Color(249, 115, 22); // #F97316 Amber/Orange
    public static final Color ACCENT_YELLOW = new Color(234, 179, 8); // #EAB308 Warning Yellow

    // Background
    public static final Color BG_DARK = new Color(0, 0, 0); // Pure Black `#000000`
    public static final Color BG_SURFACE = new Color(24, 24, 27); // Zinc-900 `#18181B`
    public static final Color BG_CARD = new Color(39, 39, 42); // Zinc-800 `#27272A`
    public static final Color BG_SIDEBAR = new Color(10, 10, 10); // Carbon Black `#0A0A0A`

    // Typography
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252); // Slate-50 `#F8FAFC`
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225); // Slate-300 `#CBD5E1`
    public static final Color TEXT_MUTED = new Color(148, 163, 184); // Slate-400 `#94A3B8`

    // Status
    public static final Color SUCCESS = new Color(16, 185, 129); // Emerald Green `#10B981`
    public static final Color WARNING = ACCENT_ORANGE; // Amber Orange
    public static final Color DANGER = ACCENT_RED; // Emergency Red
    public static final Color INFO = new Color(59, 130, 246); // Royal Blue `#3B82F6`

    // Borders
    public static final Color BORDER = new Color(63, 63, 70); // Zinc-700 `#3F3F46`

    // Incident status
    public static final Color STATUS_REPORTED = INFO;
    public static final Color STATUS_DISPATCHED = ACCENT;
    public static final Color STATUS_RESOLVED = SUCCESS;

    // Severities
    public static final Color SEV_LOW = SUCCESS;
    public static final Color SEV_MEDIUM = WARNING;
    public static final Color SEV_HIGH = ACCENT;
    public static final Color SEV_CRITICAL = ACCENT;

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";

    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUB = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 13);

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
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
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
        } catch (Exception ignored) {
        }

        // Custom flat button painters to remove Nimbus gradients
        javax.swing.Painter<JButton> buttonPainter = new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(object.getBackground());
                    g.fillRoundRect(0, 0, width, height, 12, 12);
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
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20),
                            Math.min(255, bg.getBlue() + 20)));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        });
        javax.swing.UIManager.put("Button[Default+MouseOver].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20),
                            Math.min(255, bg.getBlue() + 20)));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        });

        javax.swing.UIManager.put("Button[Pressed].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.max(0, bg.getRed() - 20), Math.max(0, bg.getGreen() - 20),
                            Math.max(0, bg.getBlue() - 20)));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        });
        javax.swing.UIManager.put("Button[Default+Pressed].backgroundPainter", new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = object.getBackground();
                    g.setColor(new Color(Math.max(0, bg.getRed() - 20), Math.max(0, bg.getGreen() - 20),
                            Math.max(0, bg.getBlue() - 20)));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        });

        javax.swing.Painter<JButton> disabledButtonPainter = new javax.swing.Painter<JButton>() {
            @Override
            public void paint(Graphics2D g, JButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(30, 41, 59));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        };
        javax.swing.UIManager.put("Button[Disabled].backgroundPainter", disabledButtonPainter);

        // Custom flat toggle button painters to remove Nimbus gradients
        javax.swing.Painter<JToggleButton> togglePainter = new javax.swing.Painter<JToggleButton>() {
            @Override
            public void paint(Graphics2D g, JToggleButton object, int width, int height) {
                if (object.isContentAreaFilled()) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(object.isSelected() ? ACCENT : object.getBackground());
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        };
        javax.swing.UIManager.put("ToggleButton[Enabled].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[Focused].backgroundPainter", togglePainter);
        javax.swing.UIManager.put("ToggleButton[MouseOver].backgroundPainter",
                new javax.swing.Painter<JToggleButton>() {
                    @Override
                    public void paint(Graphics2D g, JToggleButton object, int width, int height) {
                        if (object.isContentAreaFilled()) {
                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            Color bg = object.getBackground();
                            g.setColor(object.isSelected() ? ACCENT.brighter()
                                    : new Color(Math.min(255, bg.getRed() + 20), Math.min(255, bg.getGreen() + 20),
                                            Math.min(255, bg.getBlue() + 20)));
                            g.fillRoundRect(0, 0, width, height, 12, 12);
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
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(30, 41, 59));
                    g.fillRoundRect(0, 0, width, height, 12, 12);
                }
            }
        };
        javax.swing.UIManager.put("ToggleButton[Disabled].backgroundPainter", disabledTogglePainter);
        javax.swing.UIManager.put("ToggleButton[Disabled+Selected].backgroundPainter", disabledTogglePainter);

        // Custom flat rounded text field painters
        javax.swing.Painter<JComponent> textFieldPainter = new javax.swing.Painter<JComponent>() {
            @Override
            public void paint(Graphics2D g, JComponent object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g.setColor(object.isEnabled() ? object.getBackground() : BG_SURFACE);
                g.fillRoundRect(1, 1, width - 2, height - 2, 12, 12);

                // Border
                g.setColor(object.hasFocus() ? ACCENT : BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, width - 2, height - 2, 12, 12);
            }
        };
        javax.swing.UIManager.put("TextField[Enabled].backgroundPainter", textFieldPainter);
        javax.swing.UIManager.put("TextField[Focused].backgroundPainter", textFieldPainter);
        javax.swing.UIManager.put("TextField[Disabled].backgroundPainter", textFieldPainter);
        javax.swing.UIManager.put("PasswordField[Enabled].backgroundPainter", textFieldPainter);
        javax.swing.UIManager.put("PasswordField[Focused].backgroundPainter", textFieldPainter);
        javax.swing.UIManager.put("PasswordField[Disabled].backgroundPainter", textFieldPainter);

        // Custom flat rounded combo box painters
        javax.swing.Painter<JComboBox> comboBoxPainter = new javax.swing.Painter<JComboBox>() {
            @Override
            public void paint(Graphics2D g, JComboBox object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g.setColor(object.getBackground());
                g.fillRoundRect(1, 1, width - 2, height - 2, 12, 12);

                // Border
                g.setColor(object.hasFocus() ? ACCENT : BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, width - 2, height - 2, 12, 12);
            }
        };
        javax.swing.UIManager.put("ComboBox[Enabled].backgroundPainter", comboBoxPainter);
        javax.swing.UIManager.put("ComboBox[Focused].backgroundPainter", comboBoxPainter);
        javax.swing.UIManager.put("ComboBox[MouseOver].backgroundPainter", comboBoxPainter);
        javax.swing.UIManager.put("ComboBox[Pressed].backgroundPainter", comboBoxPainter);

        // ComboBox arrow button styling to remove gradients
        javax.swing.Painter<JComponent> arrowButtonPainter = new javax.swing.Painter<JComponent>() {
            @Override
            public void paint(Graphics2D g, JComponent object, int width, int height) {
                g.setColor(object.getBackground());
                g.fillRect(0, 0, width, height);
            }
        };
        javax.swing.UIManager.put("ComboBox:\"ComboBox.arrowButton\"[Enabled].backgroundPainter", arrowButtonPainter);
        javax.swing.UIManager.put("ComboBox:\"ComboBox.arrowButton\"[MouseOver].backgroundPainter", arrowButtonPainter);
        javax.swing.UIManager.put("ComboBox:\"ComboBox.arrowButton\"[Pressed].backgroundPainter", arrowButtonPainter);

        // Scrollbar styling to make thumbs flat and solid
        javax.swing.Painter<JComponent> scrollBarThumbPainter = new javax.swing.Painter<JComponent>() {
            @Override
            public void paint(Graphics2D g, JComponent object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(BORDER);
                g.fillRoundRect(2, 2, width - 4, height - 4, 6, 6);
            }
        };
        javax.swing.Painter<JComponent> scrollBarTrackPainter = new javax.swing.Painter<JComponent>() {
            @Override
            public void paint(Graphics2D g, JComponent object, int width, int height) {
                g.setColor(BG_DARK);
                g.fillRect(0, 0, width, height);
            }
        };
        javax.swing.UIManager.put("ScrollBar:ScrollBarThumb[Enabled].backgroundPainter", scrollBarThumbPainter);
        javax.swing.UIManager.put("ScrollBar:ScrollBarThumb[MouseOver].backgroundPainter", scrollBarThumbPainter);
        javax.swing.UIManager.put("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter", scrollBarTrackPainter);

        // TableHeader flat background painter to remove gradients
        javax.swing.Painter<JComponent> tableHeaderPainter = new javax.swing.Painter<JComponent>() {
            @Override
            public void paint(Graphics2D g, JComponent object, int width, int height) {
                g.setColor(BG_CARD);
                g.fillRect(0, 0, width, height);
                g.setColor(BORDER);
                g.drawLine(0, height - 1, width, height - 1);
            }
        };
        javax.swing.UIManager.put("TableHeader[Enabled].backgroundPainter", tableHeaderPainter);

        // ProgressBar flat background painter
        javax.swing.Painter<JProgressBar> progressBarBgPainter = new javax.swing.Painter<JProgressBar>() {
            @Override
            public void paint(Graphics2D g, JProgressBar object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(BG_DARK);
                g.fillRoundRect(0, 0, width, height, 8, 8);
            }
        };
        // ProgressBar flat foreground painter
        javax.swing.Painter<JProgressBar> progressBarFgPainter = new javax.swing.Painter<JProgressBar>() {
            @Override
            public void paint(Graphics2D g, JProgressBar object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(object.getForeground());
                g.fillRoundRect(0, 0, width, height, 8, 8);
            }
        };
        javax.swing.UIManager.put("ProgressBar[Enabled].backgroundPainter", progressBarBgPainter);
        javax.swing.UIManager.put("ProgressBar[Enabled].foregroundPainter", progressBarFgPainter);

        javax.swing.UIManager.put("control", BG_SURFACE);
        javax.swing.UIManager.put("info", BG_CARD);
        javax.swing.UIManager.put("nimbusBase", BG_DARK);
        javax.swing.UIManager.put("nimbusBlueGrey", BG_CARD);
        javax.swing.UIManager.put("nimbusLightBackground", BG_CARD);
        javax.swing.UIManager.put("nimbusFocus", new Color(0, 0, 0, 0));
        javax.swing.UIManager.put("nimbusSelectionBackground", ACCENT);
        javax.swing.UIManager.put("text", TEXT_PRIMARY);

        javax.swing.UIManager.put("Table.background", BG_SURFACE);
        javax.swing.UIManager.put("Table.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Table.gridColor", BORDER);
        javax.swing.UIManager.put("TableHeader.background", BG_CARD);
        javax.swing.UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
        javax.swing.UIManager.put("TableHeader.font", FONT_SMALL);

        javax.swing.UIManager.put("OptionPane.background", BG_SURFACE);
        javax.swing.UIManager.put("Panel.background", BG_SURFACE);

        javax.swing.UIManager.put("ComboBox.background", BG_SURFACE);
        javax.swing.UIManager.put("ComboBox.foreground", TEXT_PRIMARY);

        javax.swing.UIManager.put("TextField.background", BG_DARK);
        javax.swing.UIManager.put("TextField.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("TextField.caretForeground", TEXT_PRIMARY);

        javax.swing.UIManager.put("PasswordField.background", BG_DARK);
        javax.swing.UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("PasswordField.caretForeground", TEXT_PRIMARY);

        javax.swing.UIManager.put("ScrollBar.thumb", BORDER);
        javax.swing.UIManager.put("ScrollBar.track", BG_DARK);
    }
}
