package ui.user;

import database.Database;
import model.*;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReportIncidentPanel extends JPanel {

    private final UserDashboard parent;

    private JTextField tfMapCoord;
    private JTextArea  taDesc;
    private JComboBox<IncidentSeverity> cbSeverity;
    private JSpinner   spVictims;
    private JSpinner   spArea;
    private JSlider    sliderIntensity;
    private JLabel     lblIntensityValue;
    private JLabel     lblResult;
    private CityMapPanel mapPanel;
    private JLabel     lblZoom;

    private int mapX = -1, mapY = -1;
    private JPanel sidebarPanel;

    private static final String[] INTENSITY_LABELS = {
        "", "1 - Nyala kecil", "2 - Api mulai", "3 - Api rendah",
        "4 - Api sedang", "5 - Cukup besar", "6 - Api besar",
        "7 - Sangat besar", "8 - Berbahaya", "9 - Kritis", "10 - Bencana"
    };

    public ReportIncidentPanel(UserDashboard parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        mapPanel = new CityMapPanel((px, py) -> {
            mapX = px; mapY = py;
            tfMapCoord.setText(px + " , " + py);
            tfMapCoord.setForeground(UITheme.ACCENT_ORANGE);
            // Auto-buka sidebar jika sedang tersembunyi
            if (sidebarPanel != null && !sidebarPanel.isVisible())
                sidebarPanel.setVisible(true);
        });

        JScrollPane mapScroll = mapPanel.createScrollPane();
        mapScroll.setOpaque(false);

        JPanel zoomBar = buildZoomBar();
        sidebarPanel = buildSidebar();

        // Tombol buka form (muncul saat sidebar ditutup)
        JButton btnOpenForm = new JButton("📋 Form Laporan");
        btnOpenForm.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnOpenForm.setForeground(UITheme.TEXT_PRIMARY);
        btnOpenForm.setBackground(UITheme.ACCENT_RED);
        btnOpenForm.setBorderPainted(false);
        btnOpenForm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOpenForm.addActionListener(e -> sidebarPanel.setVisible(true));

        JLayeredPane layers = new JLayeredPane() {
            @Override public void doLayout() {
                int w = getWidth(), h = getHeight();
                mapScroll.setBounds(0, 0, w, h);
                int sw = 300;
                if (sidebarPanel.isVisible())
                    sidebarPanel.setBounds(w - sw, 0, sw, h);
                zoomBar.setBounds(8, 8, 110, 30);
                // tombol buka form di kanan bawah
                btnOpenForm.setBounds(w - 160, h - 48, 150, 36);
            }
        };
        layers.add(mapScroll,   JLayeredPane.DEFAULT_LAYER);
        layers.add(sidebarPanel, JLayeredPane.PALETTE_LAYER);
        layers.add(zoomBar,     JLayeredPane.PALETTE_LAYER);
        layers.add(btnOpenForm, JLayeredPane.PALETTE_LAYER);

        // Sync visibility tombol dengan sidebar
        sidebarPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentHidden(java.awt.event.ComponentEvent e) {
                btnOpenForm.setVisible(true);
                layers.revalidate(); layers.repaint();
            }
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                btnOpenForm.setVisible(false);
                layers.revalidate(); layers.repaint();
            }
        });
        btnOpenForm.setVisible(false); // awalnya tersembunyi karena sidebar terbuka

        add(layers, BorderLayout.CENTER);
    }

    private JPanel buildZoomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        bar.setBackground(new Color(10, 14, 22, 200));
        bar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        JButton btnOut = zBtn("−");
        JButton btnIn  = zBtn("+");
        JButton btnRst = zBtn("⊙");
        lblZoom = new JLabel("100%");
        lblZoom.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblZoom.setForeground(UITheme.TEXT_SECONDARY);

        btnOut.addActionListener(e -> { mapPanel.zoomOut(); updateZoomLabel(); });
        btnIn .addActionListener(e -> { mapPanel.zoomIn();  updateZoomLabel(); });
        btnRst.addActionListener(e -> { mapPanel.zoomReset(); updateZoomLabel(); });

        bar.add(btnOut);
        bar.add(lblZoom);
        bar.add(btnIn);
        bar.add(btnRst);
        return bar;
    }

    private JButton zBtn(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(UITheme.TEXT_PRIMARY);
        b.setBackground(UITheme.BG_CARD);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(22, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void updateZoomLabel() {
        if (lblZoom != null)
            lblZoom.setText(String.format("%.0f%%", mapPanel.getZoom() * 100));
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout(0, 0));
        sidebar.setBackground(new Color(12, 16, 24, 220));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 18, 10, 18));

        JLabel title = new JLabel("Laporan Kebakaran");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Klik titik di peta, isi form.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnClose.setForeground(UITheme.TEXT_SECONDARY);
        btnClose.setBackground(new Color(40, 20, 20));
        btnClose.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> sidebarPanel.setVisible(false));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(btnClose, BorderLayout.EAST);

        header.add(titleRow);
        header.add(Box.createVerticalStrut(3));
        header.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 18, 18, 18));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 0, 4, 0);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx   = 0;
        int row = 0;

        // Koordinat (read-only, diisi klik peta)
        tfMapCoord = tf();
        tfMapCoord.setEditable(false);
        tfMapCoord.setText("Klik peta untuk pilih titik...");
        tfMapCoord.setForeground(UITheme.TEXT_SECONDARY);
        row = addField(form, gc, row, "Koordinat Lokasi", tfMapCoord);

        // Severity
        cbSeverity = new JComboBox<>(IncidentSeverity.values());
        cbSeverity.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof IncidentSeverity) setText(((IncidentSeverity)v).getLabel());
                setBackground(s ? UITheme.ACCENT_RED : UITheme.BG_SURFACE);
                setForeground(UITheme.TEXT_PRIMARY);
                return this;
            }
        });
        cbSeverity.setBackground(UITheme.BG_SURFACE);
        cbSeverity.setForeground(UITheme.TEXT_PRIMARY);
        row = addField(form, gc, row, "Tingkat Kebakaran", cbSeverity);

        // Korban & Luas
        spVictims = sp(0, 0, 999, 1);
        spArea    = sp(50, 1, 99999, 10);
        gc.gridy = row++;
        JPanel twoCol = new JPanel(new GridLayout(1, 2, 8, 0));
        twoCol.setOpaque(false);
        JPanel colA = new JPanel(new BorderLayout(0, 3)); colA.setOpaque(false);
        JPanel colB = new JPanel(new BorderLayout(0, 3)); colB.setOpaque(false);
        colA.add(fLbl("Korban Terjebak"), BorderLayout.NORTH);
        colA.add(spVictims, BorderLayout.CENTER);
        colB.add(fLbl("Luas (m²)"), BorderLayout.NORTH);
        colB.add(spArea, BorderLayout.CENTER);
        twoCol.add(colA); twoCol.add(colB);
        form.add(twoCol, gc);

        // Intensitas
        sliderIntensity = new JSlider(1, 10, 5);
        sliderIntensity.setMajorTickSpacing(1);
        sliderIntensity.setPaintTicks(true);
        sliderIntensity.setPaintLabels(true);
        sliderIntensity.setSnapToTicks(true);
        sliderIntensity.setOpaque(false);
        sliderIntensity.setForeground(UITheme.TEXT_SECONDARY);

        lblIntensityValue = new JLabel(INTENSITY_LABELS[5]);
        lblIntensityValue.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblIntensityValue.setForeground(UITheme.ACCENT_ORANGE);
        sliderIntensity.addChangeListener(e -> {
            int v = sliderIntensity.getValue();
            lblIntensityValue.setText(INTENSITY_LABELS[v]);
            lblIntensityValue.setForeground(v <= 3 ? UITheme.SUCCESS : v <= 6 ? UITheme.ACCENT_ORANGE : UITheme.DANGER);
        });

        gc.gridy = row++;
        JPanel sliderWrap = new JPanel(new BorderLayout(0, 2)); sliderWrap.setOpaque(false);
        sliderWrap.add(fLbl("Intensitas Api (1–10)"), BorderLayout.NORTH);
        sliderWrap.add(sliderIntensity, BorderLayout.CENTER);
        lblIntensityValue.setHorizontalAlignment(SwingConstants.CENTER);
        sliderWrap.add(lblIntensityValue, BorderLayout.SOUTH);
        form.add(sliderWrap, gc);

        // Deskripsi
        JTextArea taDescLocal = ta(2);
        this.taDesc = taDescLocal;
        row = addField(form, gc, row, "Deskripsi", taDescLocal);

        gc.gridy = row;
        gc.weighty = 1;
        form.add(Box.createVerticalGlue(), gc);
        gc.weighty = 0;

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        lblResult = new JLabel(" ");
        lblResult.setFont(UITheme.FONT_BODY);
        lblResult.setBorder(new EmptyBorder(0, 18, 4, 18));

        RoundedButton btnSubmit = new RoundedButton("Kirim Laporan", UITheme.ACCENT_RED);
        btnSubmit.setIcon(new VectorIcon(VectorIcon.Type.TRUCK, 16, UITheme.TEXT_PRIMARY));
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnSubmit.addActionListener(e -> submit());

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(8, 18, 18, 18));
        footer.add(lblResult);
        footer.add(btnSubmit);

        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(scroll, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void submit() {
        String user = Database.getCurrentUser() != null ? Database.getCurrentUser().getUsername() : "unknown";

        if (mapX < 0) {
            lblResult.setForeground(UITheme.DANGER);
            lblResult.setText("Klik dulu titik di peta.");
            return;
        }

        // Lokasi = koordinat grid peta, tidak perlu nama manual
        String fullLoc = "Grid [" + mapX + "," + mapY + "]";
        String desc    = taDesc.getText().trim();
        if (desc.isEmpty()) desc = "Kebakaran dilaporkan oleh warga.";

        String err = IncidentService.reportIncident(
            fullLoc, (IncidentSeverity) cbSeverity.getSelectedItem(),
            desc, (int) spVictims.getValue(),
            ((Number) spArea.getValue()).intValue(),
            sliderIntensity.getValue(), user);

        if (err != null) {
            lblResult.setForeground(UITheme.DANGER);
            lblResult.setText(err);
            return;
        }

        showNotification(mapX + " , " + mapY);
        lblResult.setForeground(UITheme.SUCCESS);
        lblResult.setText("✓ Laporan terkirim!");
        reset();
    }

    private void showNotification(String coord) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Laporan Diterima", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.SUCCESS, 2, true),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)));

        JLabel icon = new JLabel();
        icon.setIcon(new VectorIcon(VectorIcon.Type.CHECK, 40, UITheme.SUCCESS));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Laporan Berhasil Dikirim!", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_SUB);
        title.setForeground(UITheme.SUCCESS);

        JLabel msg = new JLabel(
            "<html><center>Koordinat: <b>" + coord + "</b><br>Notifikasi dikirim ke petugas terdekat.</center></html>",
            SwingConstants.CENTER);
        msg.setFont(UITheme.FONT_BODY);
        msg.setForeground(UITheme.TEXT_SECONDARY);

        RoundedButton btnOk = new RoundedButton("OK", UITheme.SUCCESS);
        btnOk.setAlignmentX(CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dlg.dispose());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(msg);
        content.add(Box.createVerticalStrut(18));
        content.add(btnOk);
        for (Component c : content.getComponents())
            if (c instanceof JComponent) ((JComponent)c).setAlignmentX(CENTER_ALIGNMENT);

        panel.add(content, BorderLayout.CENTER);
        dlg.add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    public void reset() {
        tfMapCoord.setText("Klik peta untuk pilih titik...");
        tfMapCoord.setForeground(UITheme.TEXT_SECONDARY);
        taDesc.setText("");
        cbSeverity.setSelectedIndex(0);
        spVictims.setValue(0);
        spArea.setValue(50);
        sliderIntensity.setValue(5);
        lblIntensityValue.setText(INTENSITY_LABELS[5]);
        lblResult.setText(" ");
        mapX = -1; mapY = -1;
        if (mapPanel != null) mapPanel.clearSelection();
    }

    // helpers
    private int addField(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row++;
        JPanel wrap = new JPanel(new BorderLayout(0, 3));
        wrap.setOpaque(false);
        wrap.add(fLbl(label), BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        p.add(wrap, gc);
        return row;
    }

    private JLabel fLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    private JTextField tf() {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_BODY);
        f.setBackground(new Color(30, 35, 50));
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setCaretColor(UITheme.ACCENT_ORANGE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JTextArea ta(int rows) {
        JTextArea a = new JTextArea(rows, 0);
        a.setFont(UITheme.FONT_BODY);
        a.setBackground(new Color(30, 35, 50));
        a.setForeground(UITheme.TEXT_PRIMARY);
        a.setCaretColor(UITheme.ACCENT_ORANGE);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return a;
    }

    private JSpinner sp(int v, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, min, max, step));
        s.setFont(UITheme.FONT_BODY);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        return s;
    }
}
