package ui.admin;

import database.Database;
import model.*;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.StatusBadge;
import ui.components.VectorIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.PriorityQueue;
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

    private JSpinner   spTrucks;
    private JTextField tfNotes;

    private JPanel    detailOverlay;
    private JTextArea taDetail;
    private JLabel    lblDetailTitle;

    private IncidentMapPanel mapPanel;

    private Timer autoRefreshTimer;

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
        mapPanel = new IncidentMapPanel();
        // Callback: klik titik insiden di peta → pilih baris di tabel
        mapPanel.setOnIncidentClicked(row -> {
            if (row >= 0 && row < tableModel.getRowCount()) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
            }
        });
        JScrollPane mapScroll = mapPanel.createScrollPane();

        JPanel sidebar = buildSidebar();

        detailOverlay = buildDetailOverlay();
        detailOverlay.setVisible(false);

        JPanel zoomBar = buildMapZoomBar();

        JLayeredPane layers = new JLayeredPane() {
            @Override public void doLayout() {
                int w = getWidth(), h = getHeight();
                mapScroll.setBounds(0, 0, w, h);

                int sw = 360;
                sidebar.setBounds(0, 0, sw, h);

                int dw = 350;
                detailOverlay.setBounds(w - dw, 0, dw, h);

                // zoom bar pojok kanan bawah
                zoomBar.setBounds(w - 120, h - 36, 116, 28);
            }
        };
        layers.add(mapScroll,     JLayeredPane.DEFAULT_LAYER);
        layers.add(sidebar,       JLayeredPane.PALETTE_LAYER);
        layers.add(detailOverlay, JLayeredPane.MODAL_LAYER);
        layers.add(zoomBar,       JLayeredPane.MODAL_LAYER);

        add(layers, BorderLayout.CENTER);
        layers.setLayout(null);
    }
    

    private JPanel buildMapZoomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        bar.setBackground(new Color(10, 14, 22, 200));
        bar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        JLabel lblZ = new JLabel("100%");
        lblZ.setFont(new Font("SansSerif", Font.BOLD, 10));
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
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
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
        sidebar.setBackground(new Color(10, 14, 22, 225));
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
        header.add(Box.createVerticalStrut(6));
        header.add(btnRefresh);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_SMALL);
        table.setRowHeight(32);
        table.setBackground(new Color(14, 18, 28, 200));
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(new Color(39, 39, 42, 120));
        table.setSelectionBackground(new Color(80, 30, 30));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UITheme.FONT_SMALL);
        table.getTableHeader().setBackground(new Color(20, 24, 36));
        table.getTableHeader().setForeground(UITheme.ACCENT_ORANGE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setOpaque(false);
        table.getTableHeader().setOpaque(false);

        // renderer priority (#) — nomor urut prioritas, BUKAN id counter
        table.getColumnModel().getColumn(0).setMaxWidth(36);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean s, boolean f, int row, int col) {
                super.getTableCellRendererComponent(t, v, s, f, row, col);
                setHorizontalAlignment(CENTER);
                setText(row == 0 ? "🔴" : "P" + (row+1));
                setForeground(row == 0 ? UITheme.DANGER : UITheme.TEXT_SECONDARY);
                setBackground(s ? new Color(80,30,30) : new Color(0,0,0,0));
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

        JLabel lbl1 = sLbl("Jumlah kendaraan dikirim:");
        spTrucks = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        spTrucks.setFont(UITheme.FONT_BODY);
        spTrucks.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        spTrucks.setAlignmentX(LEFT_ALIGNMENT);

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
        p.add(spTrucks);
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
        panel.setBackground(new Color(10, 14, 22, 235));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        lblDetailTitle = new JLabel("Detail Insiden");
        lblDetailTitle.setFont(UITheme.FONT_SUB);
        lblDetailTitle.setForeground(UITheme.ACCENT_ORANGE);
        lblDetailTitle.setBorder(new EmptyBorder(12, 14, 8, 14));

        // Tombol tutup — dengan ActionListener yang benar
        JButton btnClose = new JButton("✕  Tutup");
        btnClose.setFont(UITheme.FONT_SMALL);
        btnClose.setForeground(UITheme.TEXT_SECONDARY);
        btnClose.setBackground(new Color(40, 20, 20));
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

        taDetail = new JTextArea();
        taDetail.setFont(UITheme.FONT_MONO);
        taDetail.setBackground(new Color(0, 0, 0, 0));
        taDetail.setForeground(UITheme.TEXT_PRIMARY);
        taDetail.setEditable(false);
        taDetail.setOpaque(false);
        taDetail.setBorder(new EmptyBorder(10, 14, 10, 14));

        JScrollPane detailScroll = new JScrollPane(taDetail);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setBorder(null);
        detailScroll.getVerticalScrollBar().setUnitIncrement(10);

        panel.add(detailHeader,  BorderLayout.NORTH);
        panel.add(detailScroll,  BorderLayout.CENTER);

        return panel;
    }

    private void closeDetail() {
        detailOverlay.setVisible(false);
        table.clearSelection();
        mapPanel.setHighlightedRow(-1);
        mapPanel.repaint();
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
        for (Incident inc : sorted) {
            tableModel.addRow(new Object[]{
                "",
                inc.getIncidentId(),
                truncate(inc.getLocation(), 18),
                inc.getSeverity(),
                inc.getFireIntensity() + "/10",
                inc.getNumVictimsTrapped() + "👤",
                inc.getStatus(),
                String.format("%.0f", inc.getPriorityScore())
            });
        }

        int available = Database.getFireStation().getAvailableTruckCount();
        int total     = Database.getFireStation().getFiretrucks().size();
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

        if (!inc.getAffectedCivilians().isEmpty()) {
            sb.append("\nKorban Terdampak:\n");
            for (Civilian c : inc.getAffectedCivilians())
                sb.append(String.format("  • %s [%s]\n", c.getName(), c.getCondition().getLabel()));
        }

        taDetail.setText(sb.toString());
        taDetail.setCaretPosition(0);
        detailOverlay.setVisible(true);
    }

    // ── Dispatch & Resolve ────────────────────────────────────────────────────
    private void dispatch() {
        Incident inc = getSelectedIncident();
        if (inc == null) { warn("Pilih insiden terlebih dahulu."); return; }
        if (inc.getStatus() == IncidentStatus.RESOLVED) { info("Insiden ini sudah selesai."); return; }

        int available = Database.getFireStation().getAvailableTruckCount();
        if (available == 0) { warn("Tidak ada kendaraan yang tersedia."); return; }

        int count  = (int) spTrucks.getValue();
        int actual = Math.min(count, available);
        inc.setStatus(IncidentStatus.DISPATCHED);
        inc.setTrucksAssigned(inc.getTrucksAssigned() + actual);
        inc.startDispatch();

        ArrayList<Firetruck> trucks = Database.getFireStation().getAvailableTrucks();
        for (int i = 0; i < Math.min(actual, trucks.size()); i++)
            trucks.get(i).setStatus(TruckStatus.DEPLOYED);

        String admin = adminName();
        JOptionPane.showMessageDialog(this,
            "<html><b>" + actual + " kendaraan</b> dikirim ke:<br>" +
            inc.getLocation() + "<br>Insiden: " + inc.getIncidentId() + "<br>Oleh: " + admin + "</html>",
            "Kendaraan Dikirim ✓", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void resolve() {
        Incident inc = getSelectedIncident();
        if (inc == null) { warn("Pilih insiden terlebih dahulu."); return; }
        if (inc.getStatus() == IncidentStatus.RESOLVED) { info("Insiden ini sudah selesai."); return; }

        String notes = tfNotes.getText().trim();
        if (notes.isBlank()) notes = "Kebakaran berhasil dipadamkan.";
        int trucksUsed = (int) spTrucks.getValue();

        int toFree = inc.getTrucksAssigned();
        ArrayList<Firetruck> deployed = new ArrayList<>();
        for (Firetruck t : Database.getFireStation().getFiretrucks())
            if (t.getStatus() == TruckStatus.DEPLOYED) deployed.add(t);
        for (int i = 0; i < Math.min(toFree, deployed.size()); i++)
            deployed.get(i).setStatus(TruckStatus.AVAILABLE);

        IncidentService.resolveIncident(inc, adminName(), Math.max(trucksUsed, inc.getTrucksAssigned()), notes);
        closeDetail();
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

    private String adminName() {
        return Database.getCurrentUser() != null ? Database.getCurrentUser().getName() : "Admin";
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "Info",       JOptionPane.INFORMATION_MESSAGE); }
    private String truncate(String s, int max) { return s.length() > max ? s.substring(0, max) + "…" : s; }
    private JLabel sLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_SMALL); l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PETA INSIDEN (background panel, scrollable+zoomable+draggable)
    // ═════════════════════════════════════════════════════════════════════════
    private class IncidentMapPanel extends JPanel {

        private BufferedImage mapImage;
        private ArrayList<Incident> incidents = new ArrayList<>();
        private int highlightedRow = -1;

        private double zoom    = 1.0;
        private static final double ZOOM_MIN  = 0.5;
        private static final double ZOOM_MAX  = 4.0;
        private static final double ZOOM_STEP = 0.15;
        private int imgW = 1000, imgH = 1000;

        // drag-to-pan
        private Point dragStart      = null;
        private Point vpAtDragStart  = null;
        private boolean dragging     = false;
        private static final int DRAG_THRESHOLD = 5;

        // callback: klik titik insiden di peta
        private java.util.function.IntConsumer onIncidentClicked;

        IncidentMapPanel() {
            setBackground(new Color(18, 22, 32));
            try {
                InputStream is = getClass().getResourceAsStream("/ui/user/citymap.png");
                if (is == null) is = getClass().getResourceAsStream("../user/citymap.png");
                if (is != null) {
                    mapImage = ImageIO.read(is);
                    imgW = mapImage.getWidth();
                    imgH = mapImage.getHeight();
                }
            } catch (Exception ignored) {}

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        dragStart = e.getPoint();
                        JScrollPane sp = getScrollPane();
                        vpAtDragStart = sp != null ? sp.getViewport().getViewPosition() : new Point(0, 0);
                        dragging = false;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart == null) return;
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    if (!dragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                        dragging = true;
                    }
                    if (dragging) {
                        JScrollPane sp = getScrollPane();
                        if (sp != null) {
                            int nx = vpAtDragStart.x - dx;
                            int ny = vpAtDragStart.y - dy;
                            sp.getViewport().setViewPosition(new Point(Math.max(0, nx), Math.max(0, ny)));
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!dragging && dragStart != null && SwingUtilities.isLeftMouseButton(e)) {
                        // Cek apakah klik tepat di atas salah satu titik insiden
                        int clicked = hitTestIncident(e.getX(), e.getY());
                        if (clicked >= 0 && onIncidentClicked != null) {
                            onIncidentClicked.accept(clicked);
                        }
                    }
                    dragStart = null;
                    vpAtDragStart = null;
                    dragging = false;
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);

            addMouseWheelListener(e -> {
                if (e.isControlDown()) {
                    double old = zoom;
                    if (e.getWheelRotation() < 0) zoom = Math.min(ZOOM_MAX, zoom + ZOOM_STEP);
                    else                           zoom = Math.max(ZOOM_MIN, zoom - ZOOM_STEP);
                    if (zoom != old) {
                        updatePreferredSize();
                        JScrollPane sp = getScrollPane();
                        if (sp != null) {
                            double ratio = zoom / old;
                            Point vp = sp.getViewport().getViewPosition();
                            int nx = (int)(e.getX() * ratio) - sp.getViewport().getWidth()/2;
                            int ny = (int)(e.getY() * ratio) - sp.getViewport().getHeight()/2;
                            sp.getViewport().setViewPosition(new Point(Math.max(0,nx), Math.max(0,ny)));
                        }
                        revalidate(); repaint();
                    }
                }
            });
        }

        void setOnIncidentClicked(java.util.function.IntConsumer cb) { this.onIncidentClicked = cb; }

        /** Kembalikan index insiden yang posisinya dekat titik (mx, my) di panel, atau -1 */
        private int hitTestIncident(int mx, int my) {
            int dw = (int)(imgW * zoom);
            int dh = (int)(imgH * zoom);
            for (int i = 0; i < incidents.size(); i++) {
                Incident inc = incidents.get(i);
                int[] coord  = parseCoord(inc.getLocation());
                int sx, sy;
                if (coord != null) {
                    sx = (int)(coord[0] / 1000.0 * dw);
                    sy = (int)(coord[1] / 1000.0 * dh);
                } else {
                    int hash = Math.abs(inc.getLocation().hashCode());
                    sx = 50 + (hash % (dw - 100));
                    sy = 50 + ((hash / (dw - 100)) % (dh - 100));
                }
                int hit = (i == highlightedRow) ? 16 : 13;
                if (Math.abs(mx - sx) <= hit && Math.abs(my - sy) <= hit) return i;
            }
            return -1;
        }

        void zoomIn()    { zoom = Math.min(ZOOM_MAX, zoom + ZOOM_STEP); updatePreferredSize(); revalidate(); repaint(); }
        void zoomOut()   { zoom = Math.max(ZOOM_MIN, zoom - ZOOM_STEP); updatePreferredSize(); revalidate(); repaint(); }
        void zoomReset() { zoom = 1.0; updatePreferredSize(); revalidate(); repaint(); }
        double getZoom() { return zoom; }

        private void updatePreferredSize() {
            setPreferredSize(new Dimension((int)(imgW * zoom), (int)(imgH * zoom)));
        }

        private JScrollPane getScrollPane() {
            Container p = getParent();
            if (p instanceof JViewport) {
                Container p2 = p.getParent();
                if (p2 instanceof JScrollPane) return (JScrollPane) p2;
            }
            return null;
        }

        JScrollPane createScrollPane() {
            updatePreferredSize();
            JScrollPane sp = new JScrollPane(this);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(null);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.getHorizontalScrollBar().setUnitIncrement(20);
            sp.getVerticalScrollBar().setUnitIncrement(20);
            return sp;
        }

        void setIncidents(ArrayList<Incident> list) { incidents = new ArrayList<>(list); }
        void setHighlightedRow(int row)             { highlightedRow = row; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int dw = (int)(imgW * zoom);
            int dh = (int)(imgH * zoom);
            int w  = getWidth(), h = getHeight();

            if (mapImage != null) {
                // isi background penuh agar tidak ada frame hitam
                g2.setColor(new Color(18, 22, 32));
                g2.fillRect(0, 0, Math.max(dw, w), Math.max(dh, h));
                g2.drawImage(mapImage, 0, 0, dw, dh, null);
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRect(0, 0, dw, dh);

                for (int i = 0; i < incidents.size(); i++) {
                    Incident inc = incidents.get(i);
                    int[] coord  = parseCoord(inc.getLocation());
                    int sx, sy;
                    if (coord != null) {
                        sx = (int)(coord[0] / 1000.0 * dw);
                        sy = (int)(coord[1] / 1000.0 * dh);
                    } else {
                        int hash = Math.abs(inc.getLocation().hashCode());
                        sx = 50 + (hash % (dw - 100));
                        sy = 50 + ((hash / (dw - 100)) % (dh - 100));
                    }

                    boolean sel = (i == highlightedRow);
                    Color col   = severityColor(inc.getSeverity());

                    if (sel) {
                        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 60));
                        g2.fillOval(sx - 22, sy - 22, 44, 44);
                    }
                    int r = sel ? 11 : 8;
                    g2.setColor(col);
                    g2.fillOval(sx - r, sy - r, r * 2, r * 2);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(sel ? 2.5f : 1.5f));
                    g2.drawOval(sx - r, sy - r, r * 2, r * 2);

                    // nomor prioritas (P1, P2, ...)
                    String lbl = String.valueOf(i + 1);
                    Font f = new Font("SansSerif", Font.BOLD, sel ? 11 : 9);
                    g2.setFont(f);
                    FontMetrics fm = g2.getFontMetrics(f);
                    g2.setColor(Color.WHITE);
                    g2.drawString(lbl, sx - fm.stringWidth(lbl)/2, sy + fm.getAscent()/2 - 1);

                    // tooltip nama lokasi jika dipilih
                    if (sel) {
                        String name = truncate(inc.getLocation().replaceAll("\\[.*?\\]","").trim(), 28);
                        Font nf = new Font("SansSerif", Font.BOLD, 11);
                        g2.setFont(nf);
                        FontMetrics nfm = g2.getFontMetrics(nf);
                        int lw = nfm.stringWidth(name) + 12, lh = nfm.getHeight() + 5;
                        int lx = Math.max(4, Math.min(sx - lw/2, w - lw - 4));
                        int ly = (sy - r - lh - 4 < 4) ? sy + r + 4 : sy - r - lh - 4;
                        g2.setColor(new Color(8, 10, 20, 210));
                        g2.fillRoundRect(lx, ly, lw, lh, 6, 6);
                        g2.setColor(col);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(lx, ly, lw, lh, 6, 6);
                        g2.setColor(Color.WHITE);
                        g2.drawString(name, lx + 6, ly + nfm.getAscent() + 2);
                    }
                }

                drawLegend(g2, dw, dh);

            } else {
                g2.setColor(new Color(40, 50, 65));
                g2.fillRect(0, 0, dw, dh);
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("citymap.png tidak ditemukan", dw/2 - 100, dh/2);
            }

            g2.dispose();
        }

        private void drawLegend(Graphics2D g2, int w, int h) {
            String[] labels = {"Kritis", "Tinggi", "Sedang", "Rendah"};
            Color[]  cols   = {UITheme.DANGER, UITheme.ACCENT_ORANGE, UITheme.WARNING, UITheme.SUCCESS};
            int lx = w - 80, ly = h - 80;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(lx - 8, ly - 6, 82, labels.length * 18 + 12, 8, 8);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(cols[i]);
                g2.fillOval(lx, ly + i * 18 + 3, 8, 8);
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.drawString(labels[i], lx + 13, ly + i * 18 + 11);
            }
        }

        private Color severityColor(IncidentSeverity s) {
            if (s == null) return UITheme.TEXT_SECONDARY;
            switch (s) {
                case CRITICAL: return UITheme.DANGER;
                case HIGH:     return UITheme.ACCENT_ORANGE;
                case MEDIUM:   return UITheme.WARNING;
                default:       return UITheme.SUCCESS;
            }
        }
    }
}
