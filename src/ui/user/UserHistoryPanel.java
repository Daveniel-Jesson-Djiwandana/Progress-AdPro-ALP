package ui.user;

import database.Database;
import model.ReportHistory;
import model.Resource;
import ui.UITheme;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class UserHistoryPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;
    private JTextArea taDetail;
    private JToggleButton btnMyOnly;

    private static final String[] COLS = {
        "ID Insiden", "Lokasi", "Tingkat", "Skor Prioritas", "Kendaraan", "Diselesaikan oleh", "Waktu Selesai"
    };

    public UserHistoryPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Riwayat Laporan Kebakaran");
        title.setIcon(new VectorIcon(VectorIcon.Type.HISTORY, 24, UITheme.TEXT_PRIMARY));
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

        btnMyOnly = new JToggleButton("Hanya laporan saya");
        btnMyOnly.setFont(UITheme.FONT_SMALL);
        btnMyOnly.setForeground(UITheme.TEXT_SECONDARY);
        btnMyOnly.setBackground(UITheme.BG_CARD);
        btnMyOnly.setBorderPainted(true);
        btnMyOnly.setFocusPainted(false);
        btnMyOnly.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMyOnly.addActionListener(e -> refresh());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(lblCount);
        header.add(left, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnMyOnly);
        rightBtns.add(btnRefresh);
        header.add(rightBtns, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(34);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(new Color(30, 58, 138, 80));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UITheme.FONT_SUB);
        table.getTableHeader().setBackground(UITheme.BG_CARD);
        table.getTableHeader().setForeground(UITheme.ACCENT_ORANGE);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail(table.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // Detail panel
        taDetail = new JTextArea(5, 30);
        taDetail.setFont(UITheme.FONT_MONO);
        taDetail.setBackground(UITheme.BG_CARD);
        taDetail.setForeground(UITheme.TEXT_PRIMARY);
        taDetail.setEditable(false);
        taDetail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        taDetail.setText("Pilih baris untuk melihat detail laporan dan sumber daya yang digunakan.");

        JScrollPane scrollDetail = new JScrollPane(taDetail);
        scrollDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER), "Detail Laporan & Sumber Daya",
            0, 0, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY));
        scrollDetail.setPreferredSize(new Dimension(0, 150));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, scrollDetail);
        split.setDividerSize(6);
        split.setResizeWeight(0.7);
        split.setBackground(UITheme.BG_DARK);
        split.setBorder(null);

        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        String myUname = Database.getCurrentUser() != null ? Database.getCurrentUser().getUsername() : "";
        boolean myOnly = btnMyOnly.isSelected();

        ArrayList<ReportHistory> history = Database.getReportHistory();
        int count = 0;
        for (ReportHistory rh : history) {
            boolean mine = rh.getIncident().getReportedBy().equals(myUname);
            if (myOnly && !mine) continue;
            tableModel.addRow(new Object[]{
                rh.getIncident().getIncidentId(),
                truncate(rh.getIncident().getLocation(), 28),
                rh.getIncident().getSeverity().getLabel(),
                String.format("%.1f", rh.getIncident().getPriorityScore()),
                rh.getTrucksDeployed() + " kendaraan",
                rh.getResolvedBy(),
                rh.getFormattedResolvedTime()
            });
            count++;
        }
        lblCount.setText(count + " insiden selesai" + (myOnly ? " (laporan Anda)" : " (semua)"));
        taDetail.setText("Pilih baris untuk melihat detail laporan dan sumber daya yang digunakan.");
    }

    private void showDetail(int row) {
        if (row < 0) return;
        String myUname = Database.getCurrentUser() != null ? Database.getCurrentUser().getUsername() : "";
        boolean myOnly = btnMyOnly.isSelected();

        ArrayList<ReportHistory> filtered = new ArrayList<>();
        for (ReportHistory rh : Database.getReportHistory()) {
            boolean mine = rh.getIncident().getReportedBy().equals(myUname);
            if (!myOnly || mine) filtered.add(rh);
        }
        if (row >= filtered.size()) return;
        ReportHistory rh = filtered.get(row);

        StringBuilder sb = new StringBuilder();
        sb.append("Insiden  : ").append(rh.getIncident().getIncidentId())
          .append("  |  Lokasi: ").append(rh.getIncident().getLocation()).append("\n");
        sb.append("Pelapor  : ").append(rh.getIncident().getReportedBy())
          .append("  |  Selesai oleh: ").append(rh.getResolvedBy()).append("\n");
        sb.append("Deskripsi: ").append(rh.getIncident().getDescription()).append("\n");
        sb.append("Catatan  : ").append(rh.getNotes()).append("\n\n");
        sb.append("Sumber Daya yang Digunakan:\n");
        for (Map.Entry<Resource, Integer> entry : rh.getResourcesUsed().entrySet()) {
            sb.append(String.format("  %-25s : %d\n",
                entry.getKey().getDisplayName(), entry.getValue()));
        }
        taDetail.setText(sb.toString());
        taDetail.setCaretPosition(0);
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
