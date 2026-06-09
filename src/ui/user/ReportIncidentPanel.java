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
import java.awt.event.ActionListener;

public class ReportIncidentPanel extends JPanel {

    private final UserDashboard parent;

    private JTextField tfMapCoord;
    private JLabel lblAddress; // HTML label — truncates with "..."
    private String fullAddress = ""; // stores the complete address text
    private boolean addressExpanded; // click-to-toggle state
    private JTextArea taDesc;
    private JComboBox<BuildingCategory> cbBuildingCat;
    private JComboBox<String> cbBuildingSubType;
    private JSpinner spVictims;

    private JLabel lblResult;
    private OsmCityMapPanel mapPanel;
    private JLabel lblZoom;

    private double mapLat = Double.NaN, mapLon = Double.NaN;
    private JPanel sidebarPanel;

    // Geocoding throttle: debounce + cache untuk menghindari HTTP 429
    private javax.swing.Timer geocodeDebounceTimer;
    private double lastGeocodedLat = Double.NaN;
    private double lastGeocodedLon = Double.NaN;
    private static final double GEOCODE_CACHE_THRESHOLD = 0.00005; // ~5 meter

    public ReportIncidentPanel(UserDashboard parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        mapPanel = new OsmCityMapPanel((lat, lon) -> {
            mapLat = lat;
            mapLon = lon;
            tfMapCoord.setText(String.format("%.5f, %.5f", lat, lon));
            tfMapCoord.setForeground(UITheme.ACCENT);
            // Auto-buka sidebar jika sedang tersembunyi
            if (sidebarPanel != null && !sidebarPanel.isVisible())
                sidebarPanel.setVisible(true);
            reverseGeocode(lat, lon);
        });

        JPanel mapScroll = mapPanel.createScrollPane();

        JPanel zoomBar = buildZoomBar();
        sidebarPanel = buildSidebar();

        JButton btnOpenForm = new JButton(" Form Laporan");
        btnOpenForm.setIcon(new VectorIcon(VectorIcon.Type.REPORT, 14, Color.WHITE));
        btnOpenForm.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        btnOpenForm.setForeground(UITheme.TEXT_PRIMARY);
        btnOpenForm.setBackground(UITheme.ACCENT_RED);
        btnOpenForm.setBorderPainted(false);
        btnOpenForm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOpenForm.addActionListener(e -> sidebarPanel.setVisible(true));

        JLayeredPane layers = new JLayeredPane() {
            @Override
            public void doLayout() {
                int w = getWidth(), h = getHeight();
                mapScroll.setBounds(0, 0, w, h);
                int sw = 360;
                if (sidebarPanel.isVisible())
                    sidebarPanel.setBounds(w - sw, 0, sw, h);
                zoomBar.setBounds(8, 8, 110, 30);
                // tombol buka form di kanan bawah
                btnOpenForm.setBounds(w - 160, h - 48, 150, 36);
            }
        };
        layers.add(mapScroll, JLayeredPane.DEFAULT_LAYER);
        layers.add(sidebarPanel, JLayeredPane.PALETTE_LAYER);
        layers.add(zoomBar, JLayeredPane.PALETTE_LAYER);
        layers.add(btnOpenForm, JLayeredPane.PALETTE_LAYER);

        // Sync visibility tombol dengan sidebar
        sidebarPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentHidden(java.awt.event.ComponentEvent e) {
                btnOpenForm.setVisible(true);
                layers.revalidate();
                layers.repaint();
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                btnOpenForm.setVisible(false);
                layers.revalidate();
                layers.repaint();
            }
        });
        btnOpenForm.setVisible(false); // awalnya tersembunyi karena sidebar terbuka

        add(layers, BorderLayout.CENTER);
    }

    private JPanel buildZoomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        bar.setBackground(new Color(10, 14, 22, 200));
        bar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        JButton btnOut = zBtn("-");
        JButton btnIn = zBtn("+");
        JButton btnRst = zBtn("o");
        lblZoom = new JLabel("100%");
        lblZoom.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        lblZoom.setForeground(UITheme.TEXT_SECONDARY);

        btnOut.addActionListener(e -> {
            mapPanel.zoomOut();
            updateZoomLabel();
        });
        btnIn.addActionListener(e -> {
            mapPanel.zoomIn();
            updateZoomLabel();
        });
        btnRst.addActionListener(e -> {
            mapPanel.zoomReset();
            updateZoomLabel();
        });

        bar.add(btnOut);
        bar.add(lblZoom);
        bar.add(btnIn);
        bar.add(btnRst);
        return bar;
    }

    private JButton zBtn(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
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

        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
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
        gc.insets = new Insets(4, 0, 4, 0);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        int row = 0;

        // Pencarian Alamat / Bangunan (Geocoding)
        JTextField tfSearch = new JTextField(15);
        tfSearch.setFont(UITheme.FONT_BODY);
        tfSearch.setBackground(UITheme.BG_DARK);
        tfSearch.setForeground(UITheme.TEXT_MUTED);
        tfSearch.setCaretColor(UITheme.ACCENT);
        tfSearch.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tfSearch.setText("Cari alamat/bangunan...");
        tfSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tfSearch.getText().equals("Cari alamat/bangunan...")) {
                    tfSearch.setText("");
                    tfSearch.setForeground(UITheme.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tfSearch.getText().isEmpty()) {
                    tfSearch.setText("Cari alamat/bangunan...");
                    tfSearch.setForeground(UITheme.TEXT_MUTED);
                }
            }
        });

        JButton btnSearch = new JButton("Cari");
        btnSearch.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        btnSearch.setForeground(UITheme.TEXT_PRIMARY);
        btnSearch.setBackground(UITheme.ACCENT);
        btnSearch.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ActionListener searchAction = e -> {
            String q = tfSearch.getText().trim();
            if (!q.isEmpty() && !q.equals("Cari alamat/bangunan...")) {
                performSearch(q);
            }
        };
        btnSearch.addActionListener(searchAction);
        tfSearch.addActionListener(searchAction);

        JPanel searchBar = new JPanel(new BorderLayout(4, 0));
        searchBar.setOpaque(false);
        searchBar.add(tfSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        row = addField(form, gc, row, "Cari Alamat / Bangunan", searchBar);

        // Koordinat (read-only, diisi klik peta atau hasil pencarian)
        tfMapCoord = tf();
        tfMapCoord.setEditable(false);
        tfMapCoord.setText("Klik peta untuk pilih titik...");
        tfMapCoord.setForeground(UITheme.TEXT_SECONDARY);
        row = addField(form, gc, row, "Koordinat Lokasi", tfMapCoord);

        // Alamat (read-only, diisi oleh geocoding — truncate + click to expand)
        lblAddress = new JLabel("Klik peta untuk deteksi alamat...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAddress.setFont(UITheme.FONT_BODY);
        lblAddress.setForeground(UITheme.TEXT_SECONDARY);
        lblAddress.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblAddress.setToolTipText("Klik untuk memperluas/memperkecil alamat");
        lblAddress.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        lblAddress.setOpaque(false);
        lblAddress.setBackground(UITheme.BG_DARK);
        addressExpanded = false;
        lblAddress.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (fullAddress != null && !fullAddress.isEmpty()) {
                    addressExpanded = !addressExpanded;
                    updateAddressLabel();
                }
            }
        });
        row = addField(form, gc, row, "Alamat Lokasi (Nominatim)", lblAddress);

        // Korban Terjebak (Luas Area diisi admin di lapangan, bukan oleh warga)
        spVictims = sp(0, 0, 999999999, 1);
        row = addField(form, gc, row, "Korban Terjebak", spVictims);

        // ── Kategori Bangunan ─────────────────────────────────────────────────
        cbBuildingCat = new JComboBox<>(BuildingCategory.values());
        cbBuildingCat.setBackground(UITheme.BG_SURFACE);
        cbBuildingCat.setForeground(UITheme.TEXT_PRIMARY);
        cbBuildingCat.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? UITheme.ACCENT : UITheme.BG_SURFACE);
                setForeground(UITheme.TEXT_PRIMARY);
                return this;
            }
        });
        row = addField(form, gc, row, "Kategori Bangunan", cbBuildingCat);

        // ── Subtipe Bangunan — diupdate otomatis saat kategori berubah ────────
        cbBuildingSubType = new JComboBox<>(BuildingCategory.BANGUNAN.getSubTypes());
        cbBuildingSubType.setBackground(UITheme.BG_SURFACE);
        cbBuildingSubType.setForeground(UITheme.TEXT_PRIMARY);
        cbBuildingSubType.setEditable(false);
        cbBuildingCat.addActionListener(e -> {
            BuildingCategory cat = (BuildingCategory) cbBuildingCat.getSelectedItem();
            if (cat != null) {
                cbBuildingSubType.setModel(new DefaultComboBoxModel<>(cat.getSubTypes()));
            }
        });
        row = addField(form, gc, row, "Jenis Bangunan", cbBuildingSubType);

        // Badge preview objek terbakar
        JLabel lblPreview = new JLabel("Contoh: " + BuildingCategory.BANGUNAN.getSubTypes()[0]
                + " (" + BuildingCategory.BANGUNAN.getLabel() + ")");
        lblPreview.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 10));
        lblPreview.setForeground(UITheme.TEXT_MUTED);
        Runnable updatePreview = () -> {
            BuildingCategory cat = (BuildingCategory) cbBuildingCat.getSelectedItem();
            Object subType = cbBuildingSubType.getSelectedItem();
            if (cat != null && subType != null)
                lblPreview.setText("Objek terbakar: " + subType + " (" + cat.getLabel() + ")");
        };
        cbBuildingCat.addActionListener(e -> updatePreview.run());
        cbBuildingSubType.addActionListener(e -> updatePreview.run());
        gc.gridy = row++;
        form.add(lblPreview, gc);

        // Deskripsi
        JTextArea taDescLocal = ta(2);
        this.taDesc = taDescLocal;
        row = addField(form, gc, row, "Deskripsi Singkat", taDescLocal);

        gc.gridy = row;
        gc.weighty = 1;
        form.add(Box.createVerticalGlue(), gc);
        gc.weighty = 0;

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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

        if (Double.isNaN(mapLat)) {
            lblResult.setForeground(UITheme.DANGER);
            lblResult.setText("Klik dulu titik di peta.");
            return;
        }

        // Lokasi = koordinat GPS + alamat (jika terdeteksi)
        String addrText = fullAddress != null ? fullAddress.trim() : "";
        String fullLoc;
        if (addrText.isEmpty() || addrText.equals("Klik peta untuk pilih titik...") ||
                addrText.equals("Klik peta untuk deteksi alamat...") || addrText.equals("Mencari alamat...") ||
                addrText.equals("Gagal mendapatkan alamat") || addrText.equals("Alamat tidak ditemukan")) {
            fullLoc = String.format("Lat: %.5f, Lon: %.5f", mapLat, mapLon);
        } else {
            fullLoc = String.format("Lat: %.5f, Lon: %.5f (%s)", mapLat, mapLon, addrText);
        }

        String desc = taDesc.getText().trim();
        if (desc.isEmpty())
            desc = "Kebakaran dilaporkan oleh warga.";

        IncidentSeverity severity = IncidentSeverity.UNDETERMINED;
        int intensity = 5;

        String err = IncidentService.reportIncident(
                fullLoc, severity,
                desc, (int) spVictims.getValue(),
                0, // area default 0; diisi admin di lapangan untuk Lahan Kosong
                intensity, user);

        if (err != null) {
            lblResult.setForeground(UITheme.DANGER);
            lblResult.setText(err);
            return;
        }

        // Simpan info bangunan ke incident terakhir
        Incident last = Database.getLastIncident();
        if (last != null) {
            last.setBuildingCategory((BuildingCategory) cbBuildingCat.getSelectedItem());
            last.setBuildingSubType((String) cbBuildingSubType.getSelectedItem());
        }

        showNotification(String.format("%.5f, %.5f", mapLat, mapLon));
        lblResult.setForeground(UITheme.SUCCESS);
        lblResult.setText("Laporan terkirim!");
        reset();
    }

    private void showNotification(String coord) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Laporan Diterima",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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
                "<html><center>Koordinat: <b>" + coord
                        + "</b><br>Notifikasi dikirim ke petugas terdekat.</center></html>",
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
            if (c instanceof JComponent)
                ((JComponent) c).setAlignmentX(CENTER_ALIGNMENT);

        panel.add(content, BorderLayout.CENTER);
        dlg.add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    public void reset() {
        tfMapCoord.setText("Klik peta untuk pilih titik...");
        tfMapCoord.setForeground(UITheme.TEXT_SECONDARY);
        fullAddress = "";
        addressExpanded = false;
        lblAddress.setText("Klik peta untuk deteksi alamat...");
        lblAddress.setForeground(UITheme.TEXT_SECONDARY);
        taDesc.setText("");
        cbBuildingCat.setSelectedIndex(0);
        cbBuildingSubType.setModel(new DefaultComboBoxModel<>(BuildingCategory.BANGUNAN.getSubTypes()));
        spVictims.setValue(0);

        lblResult.setText(" ");
        mapLat = Double.NaN;
        mapLon = Double.NaN;
        // Reset geocode cache
        lastGeocodedLat = Double.NaN;
        lastGeocodedLon = Double.NaN;
        if (geocodeDebounceTimer != null) geocodeDebounceTimer.stop();
        if (mapPanel != null)
            mapPanel.clearSelection();
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
        l.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 11));
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    private JTextField tf() {
        JTextField f = new JTextField(15);
        f.setFont(UITheme.FONT_BODY);
        f.setBackground(UITheme.BG_DARK);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setCaretColor(UITheme.ACCENT);
        f.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return f;
    }

    private JTextArea ta(int rows) {
        JTextArea a = new JTextArea(rows, 0) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hasFocus() ? UITheme.ACCENT : UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        a.setOpaque(false);
        a.setFont(UITheme.FONT_BODY);
        a.setBackground(UITheme.BG_DARK);
        a.setForeground(UITheme.TEXT_PRIMARY);
        a.setCaretColor(UITheme.ACCENT);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        a.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                a.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                a.repaint();
            }
        });
        return a;
    }

    /** Set the full address and update the truncated label */
    private void setAddressDisplay(String addr, Color color) {
        fullAddress = addr;
        addressExpanded = false;
        lblAddress.setForeground(color);
        updateAddressLabel();
    }

    /** Render the address label — truncated with "..." or full wrapped */
    private void updateAddressLabel() {
        int MAX_ADDR_LEN = 45;
        if (fullAddress == null || fullAddress.isEmpty()) {
            lblAddress.setText("Klik peta untuk deteksi alamat...");
            return;
        }
        if (fullAddress.equals("Mencari alamat...") || fullAddress.equals("Klik peta untuk deteksi alamat...")
                || fullAddress.equals("Klik peta untuk pilih titik...")) {
            lblAddress.setText(fullAddress);
            return;
        }
        String escaped = fullAddress.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        if (!addressExpanded && fullAddress.length() > MAX_ADDR_LEN) {
            String truncated = fullAddress.substring(0, MAX_ADDR_LEN);
            String escapedTruncated = truncated.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            lblAddress.setText("<html><body style='width:240px'>" + escapedTruncated
                    + "… <i style='color:#FFB347'>(klik untuk lihat lengkap)</i></body></html>");
        } else {
            lblAddress.setText("<html><body style='width:240px'>" + escaped + "</body></html>");
        }
        lblAddress.revalidate();
        lblAddress.repaint();
        if (sidebarPanel != null) {
            sidebarPanel.revalidate();
            sidebarPanel.repaint();
        }
    }

    private JSpinner sp(int v, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, min, max, step));
        s.setFont(UITheme.FONT_BODY);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        return s;
    }

    private void reverseGeocode(double lat, double lon) {
        // Cek cache: jika koordinat hampir sama dengan yang terakhir di-geocode, skip
        if (!Double.isNaN(lastGeocodedLat) && !Double.isNaN(lastGeocodedLon)) {
            double dLat = Math.abs(lat - lastGeocodedLat);
            double dLon = Math.abs(lon - lastGeocodedLon);
            if (dLat < GEOCODE_CACHE_THRESHOLD && dLon < GEOCODE_CACHE_THRESHOLD) {
                return; // Koordinat sama, tidak perlu request ulang
            }
        }

        fullAddress = "Mencari alamat...";
        addressExpanded = false;
        lblAddress.setText("Mencari alamat...");
        lblAddress.setForeground(UITheme.TEXT_SECONDARY);

        // Debounce: batalkan timer sebelumnya, tunggu 800ms sebelum request
        if (geocodeDebounceTimer != null && geocodeDebounceTimer.isRunning()) {
            geocodeDebounceTimer.stop();
        }
        geocodeDebounceTimer = new javax.swing.Timer(800, e -> {
            geocodeDebounceTimer.stop();
            doReverseGeocodeRequest(lat, lon, false);
        });
        geocodeDebounceTimer.setRepeats(false);
        geocodeDebounceTimer.start();
    }

    /** Lakukan HTTP request reverse geocoding (dipanggil setelah debounce) */
    private void doReverseGeocodeRequest(double lat, double lon, boolean isRetry) {
        new Thread(() -> {
            try {
                if (isRetry) {
                    Thread.sleep(2000); // Tunggu 2 detik sebelum retry setelah 429
                }
                String urlStr = String.format(
                        "https://nominatim.openstreetmap.org/reverse?lat=%.7f&lon=%.7f&format=json&addressdetails=1",
                        lat, lon);
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "SiagaKebakaran/1.0 (fire-reporting-app)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int respCode = conn.getResponseCode();
                if (respCode == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    String displayName = extractJsonString(response.toString(), "display_name");
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        final String finalAddr = displayName.trim();
                        lastGeocodedLat = lat; // Simpan ke cache
                        lastGeocodedLon = lon;
                        SwingUtilities.invokeLater(() -> {
                            setAddressDisplay(finalAddr, UITheme.TEXT_PRIMARY);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            setAddressDisplay("Alamat tidak ditemukan", UITheme.TEXT_SECONDARY);
                        });
                    }
                } else if (respCode == 429 && !isRetry) {
                    // Rate limited — retry sekali setelah 2 detik
                    SwingUtilities.invokeLater(() -> {
                        lblAddress.setText("Terlalu banyak request, mencoba ulang...");
                        lblAddress.setForeground(UITheme.TEXT_SECONDARY);
                    });
                    doReverseGeocodeRequest(lat, lon, true);
                } else {
                    SwingUtilities.invokeLater(() -> {
                        setAddressDisplay("Gagal mendapatkan alamat (HTTP " + respCode + ")", UITheme.DANGER);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setAddressDisplay("Gagal mendapatkan alamat", UITheme.DANGER);
                });
            }
        }, "ReverseGeocodeThread").start();
    }

    /** Perform forward geocoding search via Nominatim */
    private void performSearch(String query) {
        lblResult.setForeground(UITheme.TEXT_SECONDARY);
        lblResult.setText("Mencari \"" + query + "\"...");

        new Thread(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(query + " Surabaya", "UTF-8");
                String urlStr = "https://nominatim.openstreetmap.org/search?q=" + encoded
                        + "&format=json&limit=1&bounded=1&viewbox=112.55,-7.15,112.88,-7.48";
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "SiagaKebakaran/1.0 (fire-reporting-app)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int respCode = conn.getResponseCode();
                if (respCode == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null)
                        response.append(line);
                    in.close();

                    String body = response.toString().trim();
                    // Check for empty array
                    if (body.equals("[]") || body.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            lblResult.setForeground(UITheme.DANGER);
                            lblResult.setText("Lokasi \"" + query + "\" tidak ditemukan.");
                        });
                        return;
                    }

                    // Parse lat, lon, display_name from first result
                    String latStr = extractJsonString(body, "lat");
                    String lonStr = extractJsonString(body, "lon");
                    String displayName = extractJsonString(body, "display_name");

                    if (latStr == null || lonStr == null) {
                        SwingUtilities.invokeLater(() -> {
                            lblResult.setForeground(UITheme.DANGER);
                            lblResult.setText("Gagal memparsing hasil pencarian.");
                        });
                        return;
                    }

                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);

                    // Validate Surabaya bounds
                    if (lat < -7.48 || lat > -7.15 || lon < 112.55 || lon > 112.88) {
                        SwingUtilities.invokeLater(() -> {
                            lblResult.setForeground(UITheme.DANGER);
                            lblResult.setText("Lokasi di luar wilayah Surabaya.");
                        });
                        return;
                    }

                    SwingUtilities.invokeLater(() -> {
                        // Set koordinat
                        mapLat = lat;
                        mapLon = lon;
                        tfMapCoord.setText(String.format("%.5f, %.5f", lat, lon));
                        tfMapCoord.setForeground(UITheme.ACCENT);

                        // Center map and place marker
                        mapPanel.setCenterPosition(lat, lon, 17);
                        mapPanel.setSelectedLocation(lat, lon);

                        // Set address
                        if (displayName != null && !displayName.trim().isEmpty()) {
                            setAddressDisplay(displayName.trim(), UITheme.TEXT_PRIMARY);
                        } else {
                            reverseGeocode(lat, lon);
                        }

                        // Open sidebar if hidden
                        if (sidebarPanel != null && !sidebarPanel.isVisible())
                            sidebarPanel.setVisible(true);

                        lblResult.setForeground(UITheme.SUCCESS);
                        lblResult.setText("Ditemukan: " + query);
                    });
                } else if (respCode == 429) {
                    // Rate limited — tampilkan pesan dan retry sekali setelah 2 detik
                    SwingUtilities.invokeLater(() -> {
                        lblResult.setForeground(UITheme.TEXT_SECONDARY);
                        lblResult.setText("Terlalu banyak request, mencoba ulang...");
                    });
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    // Retry sekali
                    SwingUtilities.invokeLater(() -> performSearch(query));
                } else {
                    SwingUtilities.invokeLater(() -> {
                        lblResult.setForeground(UITheme.DANGER);
                        lblResult.setText("Gagal pencarian (HTTP " + respCode + ")");
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblResult.setForeground(UITheme.DANGER);
                    lblResult.setText("Gagal pencarian: " + e.getMessage());
                });
            }
        }, "SearchGeocodeThread").start();
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1)
            return null;
        start += search.length();

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'u') {
                    if (i + 4 < json.length()) {
                        try {
                            int code = Integer.parseInt(json.substring(i + 1, i + 5), 16);
                            sb.append((char) code);
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append("\\u");
                        }
                    } else {
                        sb.append("\\u");
                    }
                } else if (c == 'n') {
                    sb.append('\n');
                } else if (c == 't') {
                    sb.append('\t');
                } else if (c == 'r') {
                    sb.append('\r');
                } else {
                    sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
