package ui.user;

import database.Database;
import model.*;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.VectorIcon;

import javax.swing.*;
import java.awt.*;

public class ReportIncidentPanel extends JPanel {

    private final UserDashboard parent;
    private JTextField    tfLocation;
    private JTextArea     taDesc;
    private JComboBox<IncidentSeverity> cbSeverity;
    private JSpinner      spVictims, spArea;
    private JSlider       sliderIntensity;
    private JLabel        lblIntensityValue;
    private JLabel        lblResult;

    private static final String[] INTENSITY_DESC = {
        "", "1 — Nyala Kecil", "2 — Api Mulai", "3 — Api Rendah",
        "4 — Api Sedang", "5 — Cukup Besar", "6 — Api Besar",
        "7 — Sangat Besar", "8 — Berbahaya", "9 — Kritis", "10 — Bencana"
    };

    public ReportIncidentPanel(UserDashboard parent) {
        this.parent = parent;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Laporan Kebakaran");
        title.setIcon(new VectorIcon(VectorIcon.Type.REPORT, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING); title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Isi formulir di bawah untuk melaporkan insiden kebakaran di Surabaya.");
        sub.setFont(UITheme.FONT_BODY); sub.setForeground(UITheme.TEXT_SECONDARY);
        JPanel titlePanel = new JPanel(); titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title); titlePanel.add(Box.createVerticalStrut(4)); titlePanel.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 12);
        gc.anchor = GridBagConstraints.WEST; gc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        row = addRow(form, gc, row, "Lokasi Kejadian :", tfLocation = tf());

        cbSeverity = new JComboBox<>(IncidentSeverity.values());
        cbSeverity.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof IncidentSeverity) setText(((IncidentSeverity)v).getLabel());
                setBackground(s ? UITheme.ACCENT_RED : UITheme.BG_SURFACE);
                setForeground(UITheme.TEXT_PRIMARY); return this;
            }
        });
        cbSeverity.setBackground(UITheme.BG_SURFACE); cbSeverity.setForeground(UITheme.TEXT_PRIMARY);
        row = addRow(form, gc, row, "Tingkat Kebakaran :", cbSeverity);
        row = addRow(form, gc, row, "Jumlah Korban Terjebak :", spVictims = sp(0, 0, 100, 1));
        row = addRow(form, gc, row, "Luas Area Kebakaran (m²) :", spArea = sp(50, 0, 10000, 10));

        // ── Intensity Slider ──────────────────────────────────────────────────
        sliderIntensity = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        sliderIntensity.setMajorTickSpacing(1);
        sliderIntensity.setMinorTickSpacing(1);
        sliderIntensity.setPaintTicks(true);
        sliderIntensity.setPaintLabels(true);
        sliderIntensity.setSnapToTicks(true);
        sliderIntensity.setBackground(UITheme.BG_CARD);
        sliderIntensity.setForeground(UITheme.TEXT_SECONDARY);

        lblIntensityValue = new JLabel(INTENSITY_DESC[5]);
        lblIntensityValue.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIntensityValue.setForeground(UITheme.ACCENT_ORANGE);

        sliderIntensity.addChangeListener(e -> {
            int v = sliderIntensity.getValue();
            lblIntensityValue.setText(INTENSITY_DESC[v]);
            Color c = v <= 3 ? UITheme.SUCCESS :
                      v <= 6 ? UITheme.ACCENT_ORANGE : UITheme.DANGER;
            lblIntensityValue.setForeground(c);
        });

        // Slider row: label | slider + value label
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        form.add(fLbl("Intensitas Api (1–10) :"), gc);
        gc.gridx = 1; gc.gridwidth = 2; gc.weightx = 1;
        JPanel sliderPanel = new JPanel(new BorderLayout(8, 0));
        sliderPanel.setOpaque(false);
        sliderPanel.add(sliderIntensity, BorderLayout.CENTER);
        sliderPanel.add(lblIntensityValue, BorderLayout.EAST);
        form.add(sliderPanel, gc);
        row++;

        // Description
        gc.gridx=0; gc.gridy=row; gc.gridwidth=1;
        form.add(fLbl("Deskripsi Singkat :"), gc);
        gc.gridx=1; gc.gridwidth=2; gc.weightx=1;
        taDesc = new JTextArea(3, 20);
        taDesc.setFont(UITheme.FONT_BODY); taDesc.setBackground(UITheme.BG_SURFACE);
        taDesc.setForeground(UITheme.TEXT_PRIMARY); taDesc.setCaretColor(UITheme.ACCENT_ORANGE);
        taDesc.setLineWrap(true); taDesc.setWrapStyleWord(true);
        taDesc.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane spDesc = new JScrollPane(taDesc);
        spDesc.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        form.add(spDesc, gc);

        lblResult = new JLabel(" ");
        lblResult.setFont(UITheme.FONT_BODY);

        RoundedButton btnSubmit = new RoundedButton("  Kirim Laporan", UITheme.ACCENT_RED);
        btnSubmit.setIcon(new VectorIcon(VectorIcon.Type.TRUCK, 16, UITheme.TEXT_PRIMARY));
        btnSubmit.setPreferredSize(new Dimension(200, 42));
        btnSubmit.addActionListener(e -> submit());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false); btnPanel.add(lblResult); btnPanel.add(btnSubmit);

        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.add(titlePanel, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    private void submit() {
        String by = Database.getCurrentUser() != null ? Database.getCurrentUser().getUsername() : "unknown";
        String err = IncidentService.reportIncident(
            tfLocation.getText().trim(),
            (IncidentSeverity) cbSeverity.getSelectedItem(),
            taDesc.getText().trim(),
            (int) spVictims.getValue(),
            ((Number) spArea.getValue()).intValue(),
            sliderIntensity.getValue(), by);
        if (err != null) { lblResult.setForeground(UITheme.DANGER); lblResult.setText(err); }
        else { lblResult.setForeground(UITheme.SUCCESS); lblResult.setText("✓ Laporan berhasil dikirim!"); reset(); }
    }

    public void reset() {
        tfLocation.setText(""); taDesc.setText(""); cbSeverity.setSelectedIndex(0);
        spVictims.setValue(0); spArea.setValue(50);
        sliderIntensity.setValue(5);
        lblIntensityValue.setText(INTENSITY_DESC[5]);
        lblResult.setText(" ");
    }

    private int addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0; p.add(fLbl(label), gc);
        gc.gridx=1; gc.gridwidth=2; gc.weightx=1; p.add(field, gc); return row+1;
    }
    private JLabel fLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_BODY); l.setForeground(UITheme.TEXT_SECONDARY); return l;
    }
    private JTextField tf() {
        JTextField f = new JTextField(); f.setFont(UITheme.FONT_BODY);
        f.setBackground(UITheme.BG_SURFACE); f.setForeground(UITheme.TEXT_PRIMARY);
        f.setCaretColor(UITheme.ACCENT_ORANGE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER,1,true),
            BorderFactory.createEmptyBorder(6,8,6,8))); return f;
    }
    private JSpinner sp(int v, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v,min,max,step));
        s.setFont(UITheme.FONT_BODY);
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        return s;
    }
}
