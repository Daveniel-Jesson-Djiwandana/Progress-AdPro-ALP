package ui.user;

import database.Database;
import model.Incident;
import model.IncidentSeverity;
import model.IncidentStatus;
import ui.UITheme;
import ui.components.StatusBadge;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ViewStatusPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;
    private JLabel lblLastUpdate;
    private Timer autoRefreshTimer;

    private static final String[] COLS = {
        "ID", "Lokasi", "Tingkat", "Intensitas", "Korban", "Progress", "Status", "Dilaporkan"
    };

    public ViewStatusPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Status Laporan Aktif");
        title.setIcon(new VectorIcon(VectorIcon.Type.STATUS, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Memuat...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        lblLastUpdate = new JLabel("Diperbarui otomatis setiap 3 detik");
        lblLastUpdate.setFont(UITheme.FONT_SMALL);
        lblLastUpdate.setForeground(UITheme.TEXT_MUTED);

        JButton btnRefresh = new JButton(" Perbarui");
        btnRefresh.setIcon(new VectorIcon(VectorIcon.Type.REFRESH, 14, UITheme.ACCENT_ORANGE));
        btnRefresh.setFont(UITheme.FONT_SMALL);
        btnRefresh.setForeground(UITheme.ACCENT_ORANGE);
        btnRefresh.setBackground(UITheme.BG_CARD);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refresh());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(lblCount);
        left.add(lblLastUpdate);
        header.add(left, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        // Info banner
        JPanel infoBanner = new JPanel(new BorderLayout());
        infoBanner.setBackground(new Color(30, 58, 138, 60));
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.INFO, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel infoText = new JLabel(
            "<html>ℹ  Halaman ini menampilkan status semua laporan kebakaran aktif. " +
            "Status diperbarui secara real-time oleh petugas pemadam kebakaran.</html>");
        infoText.setFont(UITheme.FONT_SMALL);
        infoText.setForeground(UITheme.INFO);
        infoBanner.add(infoText);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 5 ? Integer.class : Object.class;
            }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(36);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(new Color(30, 58, 138, 80));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UITheme.FONT_SUB);
        table.getTableHeader().setBackground(UITheme.BG_CARD);
        table.getTableHeader().setForeground(UITheme.ACCENT_ORANGE);

        // Severity renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentSeverity) return StatusBadge.forSeverity((IncidentSeverity) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });

        // Progress bar renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new TableCellRenderer() {
            private final JProgressBar bar = new JProgressBar(0, 100);
            {
                bar.setStringPainted(true);
                bar.setFont(UITheme.FONT_SMALL);
                bar.setBackground(UITheme.BG_CARD);
            }
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                int pct = (val instanceof Integer) ? (Integer) val : 0;
                bar.setValue(pct);
                bar.setString(pct + "%");
                bar.setForeground(pct >= 80 ? UITheme.SUCCESS :
                                  pct >= 40 ? UITheme.ACCENT_ORANGE : UITheme.INFO);
                bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                return bar;
            }
        });

        // Status renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentStatus) return StatusBadge.forStatus((IncidentStatus) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });

        int[] widths = {80, 200, 75, 70, 60, 120, 100, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setOpaque(false);
        centre.add(infoBanner, BorderLayout.NORTH);
        centre.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(3000, e -> refresh());
        autoRefreshTimer.start();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        int count = 0;
        for (Incident inc : Database.getAllIncidents()) {
            if (inc.getStatus() != IncidentStatus.RESOLVED) {
                tableModel.addRow(new Object[]{
                    inc.getIncidentId(),
                    truncate(inc.getLocation(), 28),
                    inc.getSeverity(),
                    inc.getFireIntensity() + "/10",
                    inc.getNumVictimsTrapped() + " org",
                    inc.getDispatchProgress(),
                    inc.getStatus(),
                    inc.getFormattedTime()
                });
                count++;
            }
        }
        lblCount.setText(count + " insiden aktif saat ini");
        lblLastUpdate.setText("Terakhir diperbarui: " +
            new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
