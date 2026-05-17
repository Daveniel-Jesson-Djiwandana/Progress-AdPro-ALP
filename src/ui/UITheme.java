package ui;

import java.awt.*;

public class UITheme {
    // Background colors
    public static final Color BG_DARK = new Color(9, 9, 11);
    public static final Color BG_SURFACE = new Color(9, 9, 11);
    public static final Color BG_CARD = new Color(24, 24, 27);
    public static final Color BG_SIDEBAR = new Color(9, 9, 11);

    // SECONDARY COLORS
    public static final Color ACCENT_RED = new Color(239, 68, 68);
    public static final Color ACCENT_ORANGE = new Color(249, 115, 22);
    public static final Color ACCENT_YELLOW = new Color(234, 179, 8);

    // Typography colors
    public static final Color TEXT_PRIMARY = new Color(250, 250, 250);
    public static final Color TEXT_SECONDARY = new Color(161, 161, 170);
    public static final Color TEXT_MUTED = new Color(82, 82, 91);

    // Status / Semantic
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(234, 179, 8);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color INFO = new Color(59, 130, 246);

    // Very subtle borders
    public static final Color BORDER = new Color(39, 39, 42);

    // Incident status
    public static final Color STATUS_REPORTED = INFO;
    public static final Color STATUS_DISPATCHED = ACCENT_ORANGE;
    public static final Color STATUS_RESOLVED = SUCCESS;

    // Severities
    public static final Color SEV_LOW = SUCCESS;
    public static final Color SEV_MEDIUM = INFO;
    public static final Color SEV_HIGH = ACCENT_ORANGE;
    public static final Color SEV_CRITICAL = DANGER;

    // Fonts
    private static final String FONT_FAMILY = "SansSerif";

    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUB = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 13);

    // Apply the theme
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

        // Global Overrides
        javax.swing.UIManager.put("control", BG_SURFACE);
        javax.swing.UIManager.put("info", BG_CARD);
        javax.swing.UIManager.put("nimbusBase", BG_DARK);
        javax.swing.UIManager.put("nimbusBlueGrey", BG_CARD);
        javax.swing.UIManager.put("nimbusLightBackground", BG_CARD);
        javax.swing.UIManager.put("nimbusFocus", new Color(0, 0, 0, 0));
        javax.swing.UIManager.put("nimbusSelectionBackground", BORDER);
        javax.swing.UIManager.put("text", TEXT_PRIMARY);

        // Table styling (Flat and simple)
        javax.swing.UIManager.put("Table.background", BG_SURFACE);
        javax.swing.UIManager.put("Table.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("Table.gridColor", BORDER);
        javax.swing.UIManager.put("TableHeader.background", BG_CARD);
        javax.swing.UIManager.put("TableHeader.foreground", TEXT_SECONDARY);
        javax.swing.UIManager.put("TableHeader.font", FONT_SMALL);

        // Panels
        javax.swing.UIManager.put("OptionPane.background", BG_SURFACE);
        javax.swing.UIManager.put("Panel.background", BG_SURFACE);

        // Input fields (Flat, subtle border)
        javax.swing.UIManager.put("TextField.background", BG_DARK);
        javax.swing.UIManager.put("TextField.foreground", TEXT_PRIMARY);
        javax.swing.UIManager.put("TextField.caretForeground", TEXT_PRIMARY);

        // Scrollbars
        javax.swing.UIManager.put("ScrollBar.thumb", BORDER);
        javax.swing.UIManager.put("ScrollBar.track", BG_DARK);
    }
}
