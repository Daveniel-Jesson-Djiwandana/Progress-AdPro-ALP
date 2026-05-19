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
import java.util.ArrayList;

public class ViewStatusPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;

    private static final String[] COLS = {
        "ID", "Lokasi", "Tingkat", "Intensitas", "Korban", "Status", "Dilaporkan"
    };

    public ViewStatusPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Status Laporan Aktif");
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
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(34);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(new Color(70, 35, 35));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UITheme.FONT_SUB);
        table.getTableHeader().setBackground(UITheme.BG_CARD);
        table.getTableHeader().setForeground(UITheme.ACCENT_ORANGE);

        // Severity renderer (col 2)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentSeverity) return StatusBadge.forSeverity((IncidentSeverity) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });
        // Status renderer (col 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                if (val instanceof IncidentStatus) return StatusBadge.forStatus((IncidentStatus) val);
                return super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        String myUname = Database.getCurrentUser() != null ? Database.getCurrentUser().getUsername() : "";
        int count = 0;
        for (Incident inc : Database.getAllIncidents()) {
            if (inc.getStatus() != IncidentStatus.RESOLVED && inc.getReportedBy().equals(myUname)) {
                tableModel.addRow(new Object[]{
                    inc.getIncidentId(),
                    inc.getLocation(),
                    inc.getSeverity(),
                    inc.getFireIntensity() + "/10",
                    inc.getNumVictimsTrapped() + " orang",
                    inc.getStatus(),
                    inc.getFormattedTime()
                });
                count++;
            }
        }
        lblCount.setText(count + " laporan aktif Anda");
    }
}
