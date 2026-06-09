package ui.admin;

import model.Incident;
import model.IncidentSeverity;
import model.IncidentStatus;
import service.IncidentService;
import ui.UITheme;
import ui.components.StatusBadge;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

//Admin Status
public class AdminStatusPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;
    private Timer autoRefreshTimer;

    private static final String[] COLS = {
        "ID", "Lokasi", "Tingkat", "Intensitas", "Durasi", "Progress", "Status", "Skor"
    };

    public AdminStatusPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Status Semua Insiden (Admin)");
        title.setIcon(new VectorIcon(VectorIcon.Type.STATUS, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Memuat...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

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
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title); left.add(lblCount);
        header.add(left, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                // col 5 = progress (Integer for JProgressBar renderer)
                return c == 5 ? Integer.class : Object.class;
            }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(36);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.ACCENT);
        table.setShowVerticalLines(false);
        UITheme.styleTableHeader(table, UITheme.FONT_SUB);

        // Severity renderer (col 2)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentSeverity) return StatusBadge.forSeverity((IncidentSeverity) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });

        // Progress bar renderer (col 5)
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
                if (pct > 0 && pct <= 30) {
                    bar.setString("Perjalanan (" + pct + "%)");
                    bar.setForeground(UITheme.INFO);
                } else if (pct > 30) {
                    bar.setString("Pemadaman (" + pct + "%)");
                    bar.setForeground(pct >= 80 ? UITheme.SUCCESS : UITheme.ACCENT);
                } else {
                    bar.setString(pct + "%");
                    bar.setForeground(UITheme.INFO);
                }
                bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                return bar;
            }
        });

        // Status renderer (col 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentStatus) return StatusBadge.forStatus((IncidentStatus) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });

        // Column widths
        int[] widths = {80, 220, 75, 70, 90, 120, 95, 60};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(1000, e -> refresh());
        autoRefreshTimer.start();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        ArrayList<Incident> all = IncidentService.getActiveIncidents();
        for (Incident inc : all) {
            tableModel.addRow(new Object[]{
                inc.getIncidentId(),
                truncate(inc.getLocation(), 30),
                inc.getSeverity(),
                inc.getFireIntensity() + "/10",
                inc.getFormattedDuration(),
                inc.getDispatchProgress(),
                inc.getStatus(),
                String.format("%.1f", inc.getPriorityScore())
            });
        }
        lblCount.setText(all.size() + " insiden aktif");
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
