package ui.admin;

import database.Database;
import model.DispatchPriorityRules;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * PriorityRulesPanel — Configure Dispatch Priority Rules (Fitur 3.7)
 *
 * Admin can adjust victimWeight, areaWeight, intensityWeight via sliders.
 * Live preview shows how the formula scores a sample incident.
 * "Simpan & Terapkan" rebuilds the PriorityQueue with new weights.
 */
public class PriorityRulesPanel extends JPanel {

    private JSlider slVictim, slArea, slIntensity;
    private JLabel lblVictimVal, lblAreaVal, lblIntensityVal;
    private JLabel lblPreviewScore, lblPreviewTrucks;
    private JLabel lblFormula;

    // Sample incident values for live preview
    private static final int SAMPLE_VICTIMS   = 5;
    private static final double SAMPLE_AREA   = 200.0;
    private static final int SAMPLE_INTENSITY = 7;

    public PriorityRulesPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        loadCurrentValues();
    }

    private void buildUI() {
        // ── Header ──
        JLabel title = new JLabel("  Aturan Prioritas Dispatch");
        title.setIcon(new VectorIcon(VectorIcon.Type.SETTINGS, 24, UITheme.ACCENT_ORANGE));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Konfigurasi bobot formula yang menentukan prioritas penanganan insiden kebakaran.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ── Centre: two-column layout ──
        JPanel centre = new JPanel(new GridLayout(1, 2, 20, 0));
        centre.setOpaque(false);

        // Left: Sliders
        RoundedPanel sliderCard = new RoundedPanel(UITheme.BG_CARD, 20);
        sliderCard.setHasBorder(true);
        sliderCard.setLayout(new BoxLayout(sliderCard, BoxLayout.Y_AXIS));
        sliderCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel sliderTitle = new JLabel("  Bobot Prioritas");
        sliderTitle.setIcon(new VectorIcon(VectorIcon.Type.SETTINGS, 16, UITheme.ACCENT_ORANGE));
        sliderTitle.setFont(UITheme.FONT_SUB);
        sliderTitle.setForeground(UITheme.ACCENT_ORANGE);
        sliderTitle.setAlignmentX(LEFT_ALIGNMENT);

        slVictim    = createSlider(0, 200, 100);  // /10 = 0.0–20.0
        slArea      = createSlider(0, 100, 5);    // /10 = 0.0–10.0
        slIntensity = createSlider(0, 200, 50);   // /10 = 0.0–20.0

        lblVictimVal    = valueLabel("10.0");
        lblAreaVal      = valueLabel("0.5");
        lblIntensityVal = valueLabel("5.0");

        slVictim.addChangeListener(e -> {
            lblVictimVal.setText(String.format("%.1f", slVictim.getValue() / 10.0));
            updatePreview();
        });
        slArea.addChangeListener(e -> {
            lblAreaVal.setText(String.format("%.1f", slArea.getValue() / 10.0));
            updatePreview();
        });
        slIntensity.addChangeListener(e -> {
            lblIntensityVal.setText(String.format("%.1f", slIntensity.getValue() / 10.0));
            updatePreview();
        });

        sliderCard.add(sliderTitle);
        sliderCard.add(Box.createVerticalStrut(16));
        sliderCard.add(buildSliderRow("Bobot Korban (Nyawa)", slVictim, lblVictimVal,
                "Prioritaskan insiden dengan banyak korban"));
        sliderCard.add(Box.createVerticalStrut(14));
        sliderCard.add(buildSliderRow("Bobot Luas Area", slArea, lblAreaVal,
                "Prioritaskan insiden dengan area luas"));
        sliderCard.add(Box.createVerticalStrut(14));
        sliderCard.add(buildSliderRow("Bobot Intensitas Api", slIntensity, lblIntensityVal,
                "Prioritaskan insiden dengan intensitas tinggi"));

        // Right: Preview + Actions
        RoundedPanel previewCard = new RoundedPanel(UITheme.BG_CARD, 20);
        previewCard.setHasBorder(true);
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));
        previewCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel previewTitle = new JLabel("  Pratinjau Langsung");
        previewTitle.setIcon(new VectorIcon(VectorIcon.Type.STATUS, 16, UITheme.INFO));
        previewTitle.setFont(UITheme.FONT_SUB);
        previewTitle.setForeground(UITheme.INFO);
        previewTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Formula display
        lblFormula = new JLabel();
        lblFormula.setFont(UITheme.FONT_MONO);
        lblFormula.setForeground(UITheme.TEXT_SECONDARY);
        lblFormula.setAlignmentX(LEFT_ALIGNMENT);

        // Sample scenario
        JLabel lblSample = new JLabel("<html><b>Skenario Contoh:</b><br>"
                + "Korban: " + SAMPLE_VICTIMS + " orang | Luas: " + (int) SAMPLE_AREA
                + " m² | Intensitas: " + SAMPLE_INTENSITY + "/10</html>");
        lblSample.setFont(UITheme.FONT_BODY);
        lblSample.setForeground(UITheme.TEXT_SECONDARY);
        lblSample.setAlignmentX(LEFT_ALIGNMENT);

        // Score result
        lblPreviewScore = new JLabel("Skor: --");
        lblPreviewScore.setFont(new Font("SansSerif", Font.BOLD, 36));
        lblPreviewScore.setForeground(UITheme.ACCENT_ORANGE);
        lblPreviewScore.setAlignmentX(LEFT_ALIGNMENT);

        lblPreviewTrucks = new JLabel("Rekomendasi: -- truk");
        lblPreviewTrucks.setFont(UITheme.FONT_SUB);
        lblPreviewTrucks.setForeground(UITheme.SUCCESS);
        lblPreviewTrucks.setAlignmentX(LEFT_ALIGNMENT);

        // Action buttons
        RoundedButton btnSave = new RoundedButton("  Simpan & Terapkan", UITheme.SUCCESS);
        btnSave.setIcon(new VectorIcon(VectorIcon.Type.CHECK, 16, Color.WHITE));
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> saveRules());

        RoundedButton btnReset = new RoundedButton("  Reset ke Default", UITheme.BG_CARD);
        btnReset.setIcon(new VectorIcon(VectorIcon.Type.REFRESH, 16, UITheme.TEXT_PRIMARY));
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnReset.setAlignmentX(LEFT_ALIGNMENT);
        btnReset.addActionListener(e -> resetRules());

        previewCard.add(previewTitle);
        previewCard.add(Box.createVerticalStrut(12));
        previewCard.add(lblFormula);
        previewCard.add(Box.createVerticalStrut(12));
        previewCard.add(lblSample);
        previewCard.add(Box.createVerticalStrut(16));
        previewCard.add(lblPreviewScore);
        previewCard.add(Box.createVerticalStrut(4));
        previewCard.add(lblPreviewTrucks);
        previewCard.add(Box.createVerticalGlue());
        previewCard.add(btnSave);
        previewCard.add(Box.createVerticalStrut(8));
        previewCard.add(btnReset);

        centre.add(sliderCard);
        centre.add(previewCard);

        // ── Bottom: Current Active Rules ──
        RoundedPanel infoCard = new RoundedPanel(new Color(20, 25, 40), 16);
        infoCard.setHasBorder(true);
        infoCard.setLayout(new BorderLayout());
        infoCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        infoCard.setPreferredSize(new Dimension(0, 80));

        JLabel infoTitle = new JLabel("  Bagaimana Prioritas Bekerja");
        infoTitle.setIcon(new VectorIcon(VectorIcon.Type.ALERT, 16, UITheme.INFO));
        infoTitle.setFont(UITheme.FONT_SUB);
        infoTitle.setForeground(UITheme.INFO);

        JLabel infoDesc = new JLabel("<html>Sistem menggunakan <b>Priority Queue</b> "
                + "untuk mengurutkan insiden. Skor dihitung: "
                + "<b>Score = (bobotKorban × jumlah_korban) + (bobotArea × luas/10) + (bobotIntensitas × intensitas)</b>. "
                + "Insiden dengan skor tertinggi diprioritaskan. Jumlah truk yang dikirim "
                + "ditentukan otomatis: ≥100→4 truk, ≥60→3, ≥30→2, lainnya→1.</html>");
        infoDesc.setFont(UITheme.FONT_SMALL);
        infoDesc.setForeground(UITheme.TEXT_SECONDARY);

        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(infoDesc, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(infoCard, BorderLayout.SOUTH);

        updatePreview();
    }

    // ── Slider helpers ────────────────────────────────────────────────────────

    private JSlider createSlider(int min, int max, int value) {
        JSlider sl = new JSlider(min, max, value);
        sl.setOpaque(false);
        sl.setForeground(UITheme.ACCENT_ORANGE);
        sl.setAlignmentX(LEFT_ALIGNMENT);
        sl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return sl;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(UITheme.ACCENT_ORANGE);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(60, 24));
        return l;
    }

    private JPanel buildSliderRow(String label, JSlider slider, JLabel valLabel, String tooltip) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel labels = new JPanel(new BorderLayout());
        labels.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel tip = new JLabel(tooltip);
        tip.setFont(UITheme.FONT_SMALL);
        tip.setForeground(UITheme.TEXT_MUTED);

        labels.add(lbl, BorderLayout.NORTH);
        labels.add(tip, BorderLayout.SOUTH);

        row.add(labels, BorderLayout.NORTH);

        JPanel sliderRow = new JPanel(new BorderLayout(8, 0));
        sliderRow.setOpaque(false);
        sliderRow.add(slider, BorderLayout.CENTER);
        sliderRow.add(valLabel, BorderLayout.EAST);
        row.add(sliderRow, BorderLayout.CENTER);

        return row;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    public void refresh() {
        loadCurrentValues();
    }

    private void loadCurrentValues() {
        DispatchPriorityRules rules = Database.getPriorityRules();
        slVictim.setValue((int) (rules.getVictimWeight() * 10));
        slArea.setValue((int) (rules.getAreaWeight() * 10));
        slIntensity.setValue((int) (rules.getIntensityWeight() * 10));
        lblVictimVal.setText(String.format("%.1f", rules.getVictimWeight()));
        lblAreaVal.setText(String.format("%.1f", rules.getAreaWeight()));
        lblIntensityVal.setText(String.format("%.1f", rules.getIntensityWeight()));
        updatePreview();
    }

    private void updatePreview() {
        double vw = slVictim.getValue() / 10.0;
        double aw = slArea.getValue() / 10.0;
        double iw = slIntensity.getValue() / 10.0;

        double score = (vw * SAMPLE_VICTIMS) + (aw * SAMPLE_AREA / 10.0) + (iw * SAMPLE_INTENSITY);

        lblFormula.setText(String.format(
                "<html><code>Score = (%.1f × %d) + (%.1f × %.0f/10) + (%.1f × %d)</code></html>",
                vw, SAMPLE_VICTIMS, aw, SAMPLE_AREA, iw, SAMPLE_INTENSITY));

        lblPreviewScore.setText(String.format("Skor: %.1f", score));

        int trucks;
        if (score >= 100) trucks = 4;
        else if (score >= 60) trucks = 3;
        else if (score >= 30) trucks = 2;
        else trucks = 1;
        lblPreviewTrucks.setText("Rekomendasi: " + trucks + " truk");

        Color truckColor = trucks >= 4 ? UITheme.DANGER
                         : trucks >= 3 ? UITheme.ACCENT_ORANGE
                         : trucks >= 2 ? UITheme.WARNING
                         : UITheme.SUCCESS;
        lblPreviewTrucks.setForeground(truckColor);
    }

    private void saveRules() {
        DispatchPriorityRules rules = Database.getPriorityRules();
        rules.setVictimWeight(slVictim.getValue() / 10.0);
        rules.setAreaWeight(slArea.getValue() / 10.0);
        rules.setIntensityWeight(slIntensity.getValue() / 10.0);
        Database.rebuildQueue();

        JOptionPane.showMessageDialog(this,
                "<html><b>Aturan prioritas berhasil disimpan!</b><br>"
                        + "Bobot Korban: " + String.format("%.1f", rules.getVictimWeight()) + "<br>"
                        + "Bobot Area: " + String.format("%.1f", rules.getAreaWeight()) + "<br>"
                        + "Bobot Intensitas: " + String.format("%.1f", rules.getIntensityWeight()) + "<br>"
                        + "<br>Antrian insiden telah diperbarui.</html>",
                "Aturan Disimpan ✓", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetRules() {
        Database.getPriorityRules().reset();
        Database.rebuildQueue();
        loadCurrentValues();

        JOptionPane.showMessageDialog(this,
                "<html><b>Aturan prioritas telah direset ke default!</b><br>"
                        + "Korban: 10.0 | Area: 0.5 | Intensitas: 5.0</html>",
                "Reset Berhasil ✓", JOptionPane.INFORMATION_MESSAGE);
    }
}
