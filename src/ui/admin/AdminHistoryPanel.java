package ui.admin;

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

public class AdminHistoryPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblCount;
    private JTextArea taDetail;

    private static final String[] COLS = {
            "ID Insiden", "Lokasi", "Tingkat", "Kendaraan", "Diselesaikan oleh", "Waktu Selesai"
    };

    public AdminHistoryPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Riwayat Laporan (Admin)");
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

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(lblCount);
        header.add(left, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(34);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(UITheme.ACCENT);
        table.setShowVerticalLines(false);
        UITheme.styleTableHeader(table, UITheme.FONT_SUB);

        // Detail panel shown on row selection
        taDetail = new JTextArea(5, 30);
        taDetail.setFont(UITheme.FONT_MONO);
        taDetail.setBackground(UITheme.BG_CARD);
        taDetail.setForeground(UITheme.TEXT_PRIMARY);
        taDetail.setEditable(false);
        taDetail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        taDetail.setText("Pilih baris untuk melihat detail sumber daya yang digunakan.");

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showDetail(table.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JScrollPane scrollDetail = new JScrollPane(taDetail);
        scrollDetail.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER), "Detail Sumber Daya",
                0, 0, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY));
        scrollDetail.setPreferredSize(new Dimension(0, 140));

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
        ArrayList<ReportHistory> history = Database.getReportHistory();
        for (ReportHistory rh : history) {
            tableModel.addRow(new Object[] {
                    rh.getIncident().getIncidentId(),
                    rh.getIncident().getLocation(),
                    rh.getIncident().getSeverity().getLabel(),
                    rh.getTrucksDeployed() + " kendaraan",
                    rh.getResolvedBy(),
                    rh.getFormattedResolvedTime()
            });
        }
        lblCount.setText(history.size() + " insiden selesai");
        taDetail.setText("Pilih baris untuk melihat detail sumber daya yang digunakan.");
    }

    private void showDetail(int row) {
        if (row < 0)
            return;
        ArrayList<ReportHistory> history = Database.getReportHistory();
        if (row >= history.size())
            return;
        ReportHistory rh = history.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Insiden : ").append(rh.getIncident().getIncidentId())
                .append("  |  Lokasi: ").append(rh.getIncident().getLocation()).append("\n");
        sb.append("Catatan  : ").append(rh.getNotes()).append("\n\n");
        sb.append("Sumber Daya yang Digunakan:\n");
        for (Map.Entry<Resource, Integer> entry : rh.getResourcesUsed().entrySet()) {
            sb.append(String.format("  %-25s : %d\n",
                    entry.getKey().getDisplayName(), entry.getValue()));
        }
        taDetail.setText(sb.toString());
    }
}
