package ui.admin;

import database.Database;
import model.*;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.StatusBadge;
import ui.components.VectorIcon;
import ui.user.OsmCityMapPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;

/**
 * Admin Dispatch Panel.
 * Layout: JLayeredPane — peta kota full background, sidebar tabel di kiri,
 * panel detail insiden muncul di kanan saat baris dipilih atau titik peta diklik.
 */
public class DispatchPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            lblCount;
    private JLabel            lblTrucks;
    private JToggleButton     btnInRadius;
    private JToggleButton     btnAllIncidents;

    private JLabel     lblRecVal;
    private JTextField tfNotes;

    private JPanel    detailOverlay;
    private JTextArea taDetail;
    private JLabel    lblDetailTitle;

    // Admin building-detail fields
    private JComboBox<BuildingMaterial> cbMaterial;
    private JComboBox<DamageLevel>      cbDamage;
    private JLabel                      lblBuildingInfo;

    private OsmCityMapPanel mapPanel;

    private Timer autoRefreshTimer;

    // Sidebar collapse/expand state
    private boolean sidebarVisible = true;
    private static final int SIDEBAR_W = 360;
    private JButton btnToggleSidebar;

    private static final String[] COLS = {
        "#", "ID", "Lokasi", "Tingkat", "Int.", "Korban", "Status", "Skor"
    };
    private static final Pattern COORD_PAT = Pattern.compile("\\[(\\d+),(\\d+)\\]");

    public DispatchPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        mapPanel = new OsmCityMapPanel(null);
        // Callback: klik titik insiden di peta → pilih baris di tabel
        mapPanel.setOnIncidentClicked(row -> {
            if (row >= 0 && row < tableModel.getRowCount()) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
            }
        });
        JPanel mapScroll = mapPanel.createScrollPane();

        JPanel sidebar = buildSidebar();

        detailOverlay = buildDetailOverlay();
        detailOverlay.setVisible(false);

        JPanel zoomBar = buildMapZoomBar();

        // ── Sidebar toggle tab (affix on the right edge of the sidebar) ─────
        btnToggleSidebar = new JButton("◀");
        btnToggleSidebar.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        btnToggleSidebar.setForeground(UITheme.TEXT_PRIMARY);
        btnToggleSidebar.setBackground(UITheme.BG_CARD);
        btnToggleSidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 1, UITheme.BORDER),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        btnToggleSidebar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleSidebar.setFocusPainted(false);
        btnToggleSidebar.setToolTipText("Sembunyikan / Tampilkan panel samping");

        JLayeredPane layers = new JLayeredPane() {
            @Override public void doLayout() {
                int w = getWidth(), h = getHeight();
                mapScroll.setBounds(0, 0, w, h);

                int sw = sidebarVisible ? SIDEBAR_W : 0;
                sidebar.setBounds(0, 0, sw, h);
                sidebar.setVisible(sidebarVisible);

                // Toggle tab – always visible, sits right at the sidebar's right edge
                int tabW = 20, tabH = 56;
                int tabX = sw; // flush with sidebar edge (0 when hidden)
                int tabY = (h - tabH) / 2;
                btnToggleSidebar.setBounds(tabX, tabY, tabW, tabH);
                btnToggleSidebar.setText(sidebarVisible ? "◀" : "▶");

                int dw = 350;
                detailOverlay.setBounds(w - dw, 0, dw, h);

                // zoom bar pojok kanan bawah
                zoomBar.setBounds(w - 120, h - 36, 116, 28);
            }
        };

        btnToggleSidebar.addActionListener(e -> {
            sidebarVisible = !sidebarVisible;
            layers.doLayout();
            layers.repaint();
        });

        layers.add(mapScroll,         JLayeredPane.DEFAULT_LAYER);
        layers.add(sidebar,           JLayeredPane.PALETTE_LAYER);
        layers.add(detailOverlay,     JLayeredPane.MODAL_LAYER);
        layers.add(zoomBar,           JLayeredPane.MODAL_LAYER);
        layers.add(btnToggleSidebar,  JLayeredPane.POPUP_LAYER);

        add(layers, BorderLayout.CENTER);
        layers.setLayout(null);
    }
    

    private JPanel buildMapZoomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        JLabel lblZ = new JLabel("100%");
        lblZ.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        lblZ.setForeground(UITheme.TEXT_SECONDARY);

        JButton btnOut = mkZBtn("−");
        JButton btnIn  = mkZBtn("+");
        JButton btnRst = mkZBtn("⊙");

        btnOut.addActionListener(e -> { mapPanel.zoomOut(); lblZ.setText(String.format("%.0f%%", mapPanel.getZoom()*100)); });
        btnIn .addActionListener(e -> { mapPanel.zoomIn();  lblZ.setText(String.format("%.0f%%", mapPanel.getZoom()*100)); });
        btnRst.addActionListener(e -> { mapPanel.zoomReset(); lblZ.setText("100%"); });

        bar.add(btnOut); bar.add(lblZ); bar.add(btnIn); bar.add(btnRst);
        return bar;
    }

    private JButton mkZBtn(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        b.setForeground(UITheme.TEXT_PRIMARY);
        b.setBackground(UITheme.BG_CARD);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(22, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setBackground(UITheme.BG_DARK);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 16, 8, 16));

        JLabel title = new JLabel("Dispatch Insiden");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        lblCount  = new JLabel("Memuat...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        lblTrucks = new JLabel("");
        lblTrucks.setFont(UITheme.FONT_SMALL);
        lblTrucks.setForeground(UITheme.SUCCESS);

        btnInRadius = new JToggleButton("Wilayah Saya (≤ 5km)");
        btnAllIncidents = new JToggleButton("Semua Laporan");
        btnInRadius.setSelected(true);

        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(btnInRadius);
        filterGroup.add(btnAllIncidents);

        for (JToggleButton btn : new JToggleButton[]{btnInRadius, btnAllIncidents}) {
            btn.setFont(UITheme.FONT_SMALL);
            btn.setForeground(UITheme.TEXT_SECONDARY);
            btn.setBackground(UITheme.BG_CARD);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> refresh());
        }

        JPanel filterPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(btnInRadius);
        filterPanel.add(btnAllIncidents);

        JButton btnRefresh = new JButton("⟳ Perbarui");
        btnRefresh.setFont(UITheme.FONT_SMALL);
        btnRefresh.setForeground(UITheme.ACCENT_ORANGE);
        btnRefresh.setBackground(UITheme.BG_CARD);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setAlignmentX(LEFT_ALIGNMENT);
        btnRefresh.addActionListener(e -> refresh());

        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(lblCount);
        header.add(lblTrucks);
        header.add(Box.createVerticalStrut(8));
        header.add(filterPanel);
        header.add(Box.createVerticalStrut(6));
        header.add(btnRefresh);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_SMALL);
        table.setRowHeight(32);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.ACCENT);
        table.setShowVerticalLines(false);
        UITheme.styleTableHeader(table, UITheme.FONT_SMALL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setOpaque(false);

        // renderer priority (#) — nomor urut prioritas, BUKAN id counter
        table.getColumnModel().getColumn(0).setMaxWidth(36);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean s, boolean f, int row, int col) {
                super.getTableCellRendererComponent(t, v, s, f, row, col);
                setHorizontalAlignment(CENTER);
                setText(row == 0 ? "🔴" : "P" + (row+1));
                setForeground(row == 0 ? UITheme.DANGER : UITheme.TEXT_SECONDARY);
                setBackground(s ? UITheme.ACCENT : UITheme.BG_SURFACE);
                setOpaque(true);
                return this;
            }
        });
        // renderer severity
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean s, boolean f, int row, int col) {
                if (v instanceof IncidentSeverity) return StatusBadge.forSeverity((IncidentSeverity) v);
                return super.getTableCellRendererComponent(t, v, s, f, row, col);
            }
        });
        // renderer status
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean s, boolean f, int row, int col) {
                if (v instanceof IncidentStatus) return StatusBadge.forStatus((IncidentStatus) v);
                return super.getTableCellRendererComponent(t, v, s, f, row, col);
            }
        });

        int[] widths = {36, 75, 110, 62, 36, 48, 72, 48};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
                mapPanel.setHighlightedRow(table.getSelectedRow());
                mapPanel.repaint();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UITheme.BORDER));
        tableScroll.getVerticalScrollBar().setUnitIncrement(10);

        JPanel action = buildActionPanel();

        sidebar.add(header,      BorderLayout.NORTH);
        sidebar.add(tableScroll, BorderLayout.CENTER);
        sidebar.add(action,      BorderLayout.SOUTH);

        return sidebar;
    }

    // ── Action Panel ──────────────────────────────────────────────────────────
    private JPanel buildActionPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 16, 16, 16));

        JLabel lbl1 = sLbl("Rekomendasi Kendaraan:");
        lblRecVal = new JLabel("Pilih insiden...");
        lblRecVal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRecVal.setForeground(UITheme.ACCENT_ORANGE);
        lblRecVal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        lblRecVal.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl2 = sLbl("Catatan penyelesaian:");
        tfNotes = new JTextField("Kebakaran berhasil dipadamkan.");
        tfNotes.setFont(UITheme.FONT_SMALL);
        tfNotes.setBackground(new Color(30, 35, 50));
        tfNotes.setForeground(UITheme.TEXT_PRIMARY);
        tfNotes.setCaretColor(UITheme.ACCENT_ORANGE);
        tfNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        tfNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tfNotes.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton btnDispatch = new RoundedButton("  Kirim Kendaraan", UITheme.ACCENT_ORANGE);
        btnDispatch.setIcon(new VectorIcon(VectorIcon.Type.TRUCK, 15, Color.WHITE));
        btnDispatch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnDispatch.setAlignmentX(LEFT_ALIGNMENT);
        btnDispatch.addActionListener(e -> dispatch());

        RoundedButton btnResolve = new RoundedButton("  Tandai Selesai", UITheme.SUCCESS);
        btnResolve.setIcon(new VectorIcon(VectorIcon.Type.CHECK, 15, Color.WHITE));
        btnResolve.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnResolve.setAlignmentX(LEFT_ALIGNMENT);
        btnResolve.addActionListener(e -> resolve());

        p.add(lbl1);
        p.add(Box.createVerticalStrut(4));
        p.add(lblRecVal);
        p.add(Box.createVerticalStrut(8));
        p.add(btnDispatch);
        p.add(Box.createVerticalStrut(10));
        p.add(lbl2);
        p.add(Box.createVerticalStrut(4));
        p.add(tfNotes);
        p.add(Box.createVerticalStrut(6));
        p.add(btnResolve);

        return p;
    }

    // ── Detail Overlay ────────────────────────────────────────────────────────
    private JPanel buildDetailOverlay() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        lblDetailTitle = new JLabel("Detail Insiden");
        lblDetailTitle.setFont(UITheme.FONT_SUB);
        lblDetailTitle.setForeground(UITheme.ACCENT_ORANGE);
        lblDetailTitle.setBorder(new EmptyBorder(12, 14, 8, 14));

        JButton btnClose = new JButton("✕  Tutup");
        btnClose.setFont(UITheme.FONT_SMALL);
        btnClose.setForeground(UITheme.TEXT_SECONDARY);
        btnClose.setBackground(UITheme.BG_CARD);
        btnClose.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> closeDetail());

        JPanel detailHeader = new JPanel(new BorderLayout(8, 0));
        detailHeader.setOpaque(false);
        detailHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(4, 4, 4, 8)));
        detailHeader.add(lblDetailTitle, BorderLayout.WEST);
        detailHeader.add(btnClose,       BorderLayout.EAST);

        // ── Teks detail insiden (read-only) ────────────────────────────────────
        taDetail = new JTextArea();
        taDetail.setFont(UITheme.FONT_MONO);
        taDetail.setBackground(new Color(0, 0, 0, 0));
        taDetail.setForeground(UITheme.TEXT_PRIMARY);
        taDetail.setEditable(false);
        taDetail.setOpaque(false);
        taDetail.setBorder(new EmptyBorder(10, 14, 6, 14));

        JScrollPane detailScroll = new JScrollPane(taDetail);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setBorder(null);
        detailScroll.getVerticalScrollBar().setUnitIncrement(10);

        // ── Info bangunan dari pelapor ──────────────────────────────────────
        lblBuildingInfo = new JLabel("—");
        lblBuildingInfo.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        lblBuildingInfo.setForeground(UITheme.ACCENT_ORANGE);
        lblBuildingInfo.setBorder(new EmptyBorder(0, 14, 4, 14));

        // ── Form detail admin ──────────────────────────────────────────────
        JPanel adminForm = new JPanel();
        adminForm.setOpaque(false);
        adminForm.setLayout(new BoxLayout(adminForm, BoxLayout.Y_AXIS));
        adminForm.setBorder(new EmptyBorder(6, 14, 14, 14));

        // Separator
        JLabel sep = new JLabel("───  Detail Bangunan (diisi Admin)  ───");
        sep.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        sep.setForeground(UITheme.ACCENT_ORANGE);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        adminForm.add(sep);
        adminForm.add(Box.createVerticalStrut(8));

        // Material dominan
        adminForm.add(adminLbl("🧱 Material Dominan:"));
        adminForm.add(Box.createVerticalStrut(3));
        cbMaterial = new JComboBox<>(BuildingMaterial.values());
        cbMaterial.setBackground(UITheme.BG_CARD);
        cbMaterial.setForeground(UITheme.TEXT_PRIMARY);
        cbMaterial.setFont(UITheme.FONT_SMALL);
        cbMaterial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbMaterial.setAlignmentX(LEFT_ALIGNMENT);
        cbMaterial.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof BuildingMaterial) {
                    BuildingMaterial m = (BuildingMaterial) v;
                    setText(m.getLabel() + " — " + m.getNote());
                }
                setBackground(s ? UITheme.ACCENT : UITheme.BG_CARD);
                setForeground(UITheme.TEXT_PRIMARY);
                return this;
            }
        });
        adminForm.add(cbMaterial);
        adminForm.add(Box.createVerticalStrut(10));

        // Tingkat kerusakan
        adminForm.add(adminLbl("💥 Tingkat Kerusakan:"));
        adminForm.add(Box.createVerticalStrut(3));
        cbDamage = new JComboBox<>(DamageLevel.values());
        cbDamage.setBackground(UITheme.BG_CARD);
        cbDamage.setForeground(UITheme.TEXT_PRIMARY);
        cbDamage.setFont(UITheme.FONT_SMALL);
        cbDamage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbDamage.setAlignmentX(LEFT_ALIGNMENT);
        cbDamage.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof DamageLevel) {
                    DamageLevel d = (DamageLevel) v;
                    setText(d.getLabel() + " — " + d.getDescription());
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                setBackground(s ? UITheme.ACCENT : UITheme.BG_CARD);
                return this;
            }
        });
        adminForm.add(cbDamage);
        adminForm.add(Box.createVerticalStrut(10));

        // Tombol simpan
        RoundedButton btnSave = new RoundedButton("  Simpan Detail Bangunan", UITheme.ACCENT_ORANGE);
        btnSave.setFont(UITheme.FONT_SMALL);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> saveAdminBuildingDetail());
        adminForm.add(btnSave);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(detailScroll,   BorderLayout.CENTER);
        centerPanel.add(lblBuildingInfo, BorderLayout.NORTH);

        JScrollPane adminScroll = new JScrollPane(adminForm);
        adminScroll.setOpaque(false);
        adminScroll.getViewport().setOpaque(false);
        adminScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        adminScroll.setPreferredSize(new Dimension(0, 220));

        panel.add(detailHeader, BorderLayout.NORTH);
        panel.add(centerPanel,  BorderLayout.CENTER);
        panel.add(adminScroll,  BorderLayout.SOUTH);

        return panel;
    }

    private void closeDetail() {
        detailOverlay.setVisible(false);
        table.clearSelection();
        mapPanel.setHighlightedRow(-1);
        mapPanel.repaint();
        if (lblRecVal != null) {
            lblRecVal.setText("Pilih insiden...");
            lblRecVal.setForeground(UITheme.TEXT_SECONDARY);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DATA & REFRESH
    // ═════════════════════════════════════════════════════════════════════════
    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(2000, e -> refresh());
        autoRefreshTimer.start();
    }

    public void refresh() {
        int selectedRow = table.getSelectedRow();

        ArrayList<Incident> sorted = getSortedIncidents();
        tableModel.setRowCount(0);
        FireStation adminStation = Database.getCurrentAdminStation();

        for (Incident inc : sorted) {
            String prefix = "";
            if (adminStation != null) {
                double dist = getDijkstraDistance(adminStation, inc);
                prefix = (dist <= 5.0) ? "📍 " : "🌐 ";
            }
            tableModel.addRow(new Object[]{
                "",
                prefix + inc.getIncidentId(),
                truncate(inc.getLocation(), 18),
                inc.getSeverity(),
                inc.getFireIntensity() + "/10",
                inc.getNumVictimsTrapped() + "👤",
                inc.getStatus(),
                String.format("%.0f", inc.getPriorityScore())
            });
        }

        int available = Database.getFireStation() != null ? Database.getFireStation().getAvailableTruckCount() : 0;
        int total     = Database.getFireStation() != null ? Database.getFireStation().getFiretrucks().size() : 0;
        lblCount.setText(sorted.size() + " insiden dalam antrian");
        lblTrucks.setText("Truk tersedia: " + available + " / " + total);

        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount())
            table.setRowSelectionInterval(selectedRow, selectedRow);

        mapPanel.setIncidents(sorted);
        mapPanel.repaint();
    }

    private void updateDetail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detailOverlay.setVisible(false);
            if (lblRecVal != null) {
                lblRecVal.setText("Pilih insiden...");
                lblRecVal.setForeground(UITheme.TEXT_SECONDARY);
            }
            return;
        }
        ArrayList<Incident> sorted = getSortedIncidents();
        if (row >= sorted.size()) return;
        Incident inc = sorted.get(row);

        lblDetailTitle.setText("Detail: " + inc.getIncidentId() + "  [P" + (row+1) + "]");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-13s: %s\n",  "ID",          inc.getIncidentId()));
        sb.append(String.format("%-13s: %s\n",  "Lokasi",      inc.getLocation()));
        sb.append(String.format("%-13s: %s  (Int: %d/10)\n",   "Tingkat", inc.getSeverity().getLabel(), inc.getFireIntensity()));
        sb.append(String.format("%-13s: %d orang  |  %.0f m²\n","Korban+Luas", inc.getNumVictimsTrapped(), inc.getFireSpreadArea()));
        sb.append(String.format("%-13s: %.1f  (Rek: %d truk)\n","Prioritas", inc.getPriorityScore(), inc.getRecommendedTrucks()));
        sb.append(String.format("%-13s: %s  (%s berlalu)\n",   "Waktu",    inc.getFormattedTime(), inc.getFormattedDuration()));
        sb.append(String.format("%-13s: %s\n",  "Pelapor",     inc.getReportedBy()));
        sb.append(String.format("%-13s: %s\n",  "Deskripsi",   inc.getDescription()));
        sb.append(String.format("%-13s: %d%%  (%d truk)\n",    "Progress", inc.getDispatchProgress(), inc.getTrucksAssigned()));

        // Dijkstra calculations to show pos terdekat
        double[] coords = FireStationGraph.parseGpsCoord(inc.getLocation());
        if (coords != null) {
            FireStationGraph graph = Database.getRoadNetwork();
            FireStationGraph.Node incidentNode = new FireStationGraph.Node("TempDetailNode", coords[0], coords[1]);
            graph.addNode(incidentNode);
            FireStationGraph.Node closestNode = graph.findClosestNode(coords[0], coords[1]);
            if (closestNode != null) {
                graph.addEdge("TempDetailNode", closestNode.id);
            }
            Map<FireStationGraph.Node, Double> dists = graph.dijkstra(incidentNode);
            
            FireStation closestStation = null;
            double minDist = Double.MAX_VALUE;
            double distToAdmin = Double.MAX_VALUE;
            FireStation adminStation = Database.getCurrentAdminStation();

            for (FireStation station : Database.getFireStations()) {
                FireStationGraph.Node sNode = new FireStationGraph.Node(station.getName(), station.getLatitude(), station.getLongitude());
                Double d = dists.get(sNode);
                if (d != null) {
                    if (d < minDist) {
                        minDist = d;
                        closestStation = station;
                    }
                    if (adminStation != null && station.getName().equals(adminStation.getName())) {
                        distToAdmin = d;
                    }
                }
            }
            graph.removeNode(incidentNode);

            if (closestStation != null) {
                sb.append(String.format("%-13s: %s (%.2f km)\n", "Pos Terdekat", closestStation.getName(), minDist));
            }
            if (adminStation != null && distToAdmin < Double.MAX_VALUE) {
                sb.append(String.format("%-13s: %.2f km (%s)\n", "Jarak dari Pos", distToAdmin, (distToAdmin <= 5.0 ? "Dalam Radius" : "Luar Radius")));
            }
        }

        if (inc.getNumVictimsTrapped() > 0) {
            sb.append("\nKorban Terdampak:\n");
            sb.append(String.format("  • Kritis      : %d orang\n", inc.getVictimsCritical()));
            sb.append(String.format("  • Luka-luka   : %d orang\n", inc.getVictimsInjured()));
            sb.append(String.format("  • Dievakuasi  : %d orang\n", inc.getVictimsEvacuated()));
            sb.append(String.format("  • Aman        : %d orang\n", inc.getVictimsSafe()));
        }

        taDetail.setText(sb.toString());
        taDetail.setCaretPosition(0);

        // ── Tampilkan info bangunan dari pelapor ────────────────────────────
        String bldg = inc.getBuildingLabel();
        if (inc.getBuildingCategory() != null) {
            lblBuildingInfo.setText("🏠 Objek: " + bldg);
        } else {
            lblBuildingInfo.setText("🏠 Objek terbakar: belum diisi pelapor");
        }

        // Pre-fill admin form dari data yang sudah tersimpan (jika ada)
        if (inc.getBuildingMaterial() != null) cbMaterial.setSelectedItem(inc.getBuildingMaterial());
        else cbMaterial.setSelectedIndex(0);
        if (inc.getDamageLevel() != null) cbDamage.setSelectedItem(inc.getDamageLevel());
        else cbDamage.setSelectedIndex(0);

        detailOverlay.setVisible(true);

        if (lblRecVal != null) {
            if (inc.getStatus() == IncidentStatus.RESOLVED) {
                lblRecVal.setText("Selesai (" + inc.getTrucksAssigned() + " Truk)");
                lblRecVal.setForeground(UITheme.SUCCESS);
            } else if (inc.getStatus() == IncidentStatus.DISPATCHED) {
                lblRecVal.setText("Terkirim: " + inc.getTrucksAssigned() + " Truk");
                lblRecVal.setForeground(UITheme.TEXT_SECONDARY);
            } else {
                lblRecVal.setText(inc.getRecommendedTrucks() + " Truk");
                lblRecVal.setForeground(UITheme.ACCENT_ORANGE);
            }
        }
    }

    // ── Dispatch & Resolve ────────────────────────────────────────────────────
    private void dispatch() {
        Incident inc = getSelectedIncident();
        if (inc == null) { warn("Pilih insiden terlebih dahulu."); return; }
        if (inc.getStatus() == IncidentStatus.RESOLVED) { info("Insiden ini sudah selesai."); return; }

        int count = inc.getRecommendedTrucks();
        int remaining = count;

        FireStation ownStation = Database.getFireStation();
        int ownAvailable = ownStation != null ? ownStation.getAvailableTruckCount() : 0;
        int ownDispatch = Math.min(remaining, ownAvailable);

        if (ownStation != null) {
            ArrayList<Firetruck> ownTrucks = ownStation.getAvailableTrucks();
            for (int i = 0; i < ownDispatch; i++) {
                ownTrucks.get(i).setStatus(TruckStatus.DEPLOYED);
            }
        }
        remaining -= ownDispatch;

        int backupDispatchCount = 0;
        StringBuilder backupInfo = new StringBuilder();

        if (remaining > 0) {
            double[] coords = FireStationGraph.parseGpsCoord(inc.getLocation());
            ArrayList<StationDistance> assistanceList = new ArrayList<>();

            if (coords != null) {
                FireStationGraph graph = Database.getRoadNetwork();
                FireStationGraph.Node incidentNode = new FireStationGraph.Node("TempIncident", coords[0], coords[1]);
                graph.addNode(incidentNode);
                FireStationGraph.Node closestNode = graph.findClosestNode(coords[0], coords[1]);
                if (closestNode != null) {
                    graph.addEdge("TempIncident", closestNode.id);
                }

                Map<FireStationGraph.Node, Double> dists = graph.dijkstra(incidentNode);

                for (FireStation station : Database.getFireStations()) {
                    if (ownStation != null && station.getName().equals(ownStation.getName())) continue;
                    FireStationGraph.Node sNode = new FireStationGraph.Node(station.getName(), station.getLatitude(), station.getLongitude());
                    Double d = dists.get(sNode);
                    if (d != null && station.getAvailableTruckCount() > 0) {
                        assistanceList.add(new StationDistance(station, d));
                    }
                }
                graph.removeNode(incidentNode);
            } else {
                for (FireStation station : Database.getFireStations()) {
                    if (ownStation != null && station.getName().equals(ownStation.getName())) continue;
                    if (station.getAvailableTruckCount() > 0) {
                        assistanceList.add(new StationDistance(station, 0.0));
                    }
                }
            }

            Collections.sort(assistanceList, Comparator.comparingDouble(ad -> ad.distance));

            for (StationDistance sd : assistanceList) {
                if (remaining <= 0) break;
                int avail = sd.station.getAvailableTruckCount();
                int take = Math.min(remaining, avail);

                ArrayList<Firetruck> assistTrucks = sd.station.getAvailableTrucks();
                for (int i = 0; i < take; i++) {
                    assistTrucks.get(i).setStatus(TruckStatus.DEPLOYED);
                }

                backupDispatchCount += take;
                backupInfo.append(String.format("<br>- <b>%d truk</b> dari %s (jarak: %.2f km)", take, sd.station.getName(), sd.distance));
                remaining -= take;
            }
        }

        int actualTotalDispatched = count - remaining;
        if (actualTotalDispatched == 0) {
            warn("Tidak ada kendaraan pemadam kebakaran yang tersedia di pos Anda maupun pos bantuan lainnya.");
            return;
        }


        inc.setStatus(IncidentStatus.DISPATCHED);
        inc.setTrucksAssigned(inc.getTrucksAssigned() + actualTotalDispatched);
        inc.startDispatch();

        String admin = adminName();
        String msg = "<html><b>" + ownDispatch + " kendaraan</b> dikirim dari pos Anda (" + (ownStation != null ? ownStation.getName() : "—") + ").";
        if (backupDispatchCount > 0) {
            msg += "<br>Bantuan terkirim:" + backupInfo.toString();
        }
        msg += "<br><br>Lokasi: " + inc.getLocation() + "<br>Insiden: " + inc.getIncidentId() + "<br>Oleh: " + admin + "</html>";

        JOptionPane.showMessageDialog(this, msg, "Kendaraan Dikirim ✓", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void resolve() {
        Incident inc = getSelectedIncident();
        if (inc == null) { warn("Pilih insiden terlebih dahulu."); return; }
        if (inc.getStatus() == IncidentStatus.RESOLVED) { info("Insiden ini sudah selesai."); return; }

        String notes = tfNotes.getText().trim();
        if (notes.isBlank()) notes = "Kebakaran berhasil dipadamkan.";
        int trucksUsed = inc.getTrucksAssigned();

        int toFree = inc.getTrucksAssigned();
        int freedCount = 0;
        for (FireStation station : Database.getFireStations()) {
            for (Firetruck t : station.getFiretrucks()) {
                if (t.getStatus() == TruckStatus.DEPLOYED && freedCount < toFree) {
                    t.setStatus(TruckStatus.AVAILABLE);
                    freedCount++;
                }
            }
        }

        IncidentService.resolveIncident(inc, adminName(), trucksUsed, notes);
        closeDetail();
        refresh();
        JOptionPane.showMessageDialog(this,
            "<html><b>Insiden " + inc.getIncidentId() + " selesai!</b><br>" +
            "Lokasi: " + inc.getLocation() + "</html>",
            "Insiden Selesai ✓", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private ArrayList<Incident> getSortedIncidents() {
        PriorityQueue<Incident> q = new PriorityQueue<>(Database.getIncidentQueue());
        ArrayList<Incident> list  = new ArrayList<>();
        while (!q.isEmpty()) list.add(q.poll());
        return list;
    }

    private Incident getSelectedIncident() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        ArrayList<Incident> sorted = getSortedIncidents();
        return row < sorted.size() ? sorted.get(row) : null;
    }

    static int[] parseCoord(String loc) {
        if (loc == null) return null;
        Matcher m = COORD_PAT.matcher(loc);
        if (m.find()) {
            try { return new int[]{ Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) }; }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private double getDijkstraDistance(FireStation station, Incident inc) {
        double[] coords = FireStationGraph.parseGpsCoord(inc.getLocation());
        if (coords == null) return Double.MAX_VALUE;

        FireStationGraph graph = Database.getRoadNetwork();
        FireStationGraph.Node incidentNode = new FireStationGraph.Node("TempDistNode", coords[0], coords[1]);
        graph.addNode(incidentNode);

        FireStationGraph.Node closestNode = graph.findClosestNode(coords[0], coords[1]);
        if (closestNode != null) {
            graph.addEdge("TempDistNode", closestNode.id);
        }

        Map<FireStationGraph.Node, Double> dists = graph.dijkstra(incidentNode);
        FireStationGraph.Node sNode = new FireStationGraph.Node(station.getName(), station.getLatitude(), station.getLongitude());
        Double d = dists.get(sNode);

        graph.removeNode(incidentNode);
        return d != null ? d : Double.MAX_VALUE;
    }

    private String adminName() {
        return Database.getCurrentUser() != null ? Database.getCurrentUser().getName() : "Admin";
    }

    private static class StationDistance {
        FireStation station;
        double distance;
        StationDistance(FireStation s, double d) {
            this.station = s;
            this.distance = d;
        }
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "Info",       JOptionPane.INFORMATION_MESSAGE); }
    private String truncate(String s, int max) { return s.length() > max ? s.substring(0, max) + "…" : s; }
    private JLabel sLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_SMALL); l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }
    private JLabel adminLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        l.setForeground(UITheme.ACCENT_ORANGE); l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }

    /** Simpan pilihan material & tingkat kerusakan ke incident yang sedang dipilih */
    private void saveAdminBuildingDetail() {
        Incident inc = getSelectedIncident();
        if (inc == null) { warn("Pilih insiden terlebih dahulu."); return; }
        inc.setBuildingMaterial((BuildingMaterial) cbMaterial.getSelectedItem());
        inc.setDamageLevel((DamageLevel) cbDamage.getSelectedItem());
        DamageLevel d = (DamageLevel) cbDamage.getSelectedItem();
        JOptionPane.showMessageDialog(this,
            "<html>Detail bangunan disimpan!<br>"
            + "Material: <b>" + inc.getBuildingMaterial().getLabel() + "</b><br>"
            + "Kerusakan: <b>" + (d != null ? d.getLabel() : "-") + "</b></html>",
            "Tersimpan ✓", JOptionPane.INFORMATION_MESSAGE);
    }

}
