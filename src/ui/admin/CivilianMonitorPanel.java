package ui.admin;

import database.Database;
import model.*;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

public class CivilianMonitorPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblTotal, lblCritical, lblInjured, lblEvacuated, lblSafe;
    private Timer autoRefreshTimer;

    private ArrayList<Incident> activeIncidents = new ArrayList<>();

    private static final String[] COLS = {
            "ID Insiden", "Lokasi Bangunan", "Total Korban", "Kritis", "Luka-luka", "Dievakuasi", "Aman"
    };

    public CivilianMonitorPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        // ── Header ──
        JLabel title = new JLabel("  Monitor Korban Terdampak");
        title.setIcon(new VectorIcon(VectorIcon.Type.PEOPLE, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Pantau kondisi korban yang berada di lokasi kebakaran secara real-time.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JButton btnRefresh = new JButton(" Perbarui");
        btnRefresh.setIcon(new VectorIcon(VectorIcon.Type.REFRESH, 14, UITheme.ACCENT_ORANGE));
        btnRefresh.setFont(UITheme.FONT_SMALL);
        btnRefresh.setForeground(UITheme.ACCENT_ORANGE);
        btnRefresh.setBackground(UITheme.BG_CARD);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refresh());

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.add(title);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(sub);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(headerLeft, BorderLayout.WEST);
        headerRow.add(btnRefresh, BorderLayout.EAST);

        // ── Summary Cards ──
        JPanel summaryRow = new JPanel(new GridLayout(1, 5, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        lblTotal = createSummaryCard(summaryRow, "Total Korban", "0", UITheme.TEXT_PRIMARY);
        lblCritical = createSummaryCard(summaryRow, "Kritis", "0", UITheme.DANGER);
        lblInjured = createSummaryCard(summaryRow, "Luka-luka", "0", UITheme.ACCENT_ORANGE);
        lblEvacuated = createSummaryCard(summaryRow, "Dievakuasi", "0", UITheme.INFO);
        lblSafe = createSummaryCard(summaryRow, "Aman", "0", UITheme.SUCCESS);

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(headerRow);
        topPanel.add(Box.createVerticalStrut(16));
        topPanel.add(summaryRow);

        // ── Table ──
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(36);
        table.setBackground(UITheme.BG_SURFACE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setSelectionBackground(new Color(70, 35, 35));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(UITheme.FONT_SUB);
        table.getTableHeader().setBackground(UITheme.BG_CARD);
        table.getTableHeader().setForeground(UITheme.ACCENT_ORANGE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderers to color categories
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);

        for (int i = 2; i < 7; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(80);
        }

        // Highlight columns with status colors
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row,
                    int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setForeground(UITheme.DANGER);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        });
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row,
                    int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setForeground(UITheme.ACCENT_ORANGE);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        });
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row,
                    int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setForeground(UITheme.INFO);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        });
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row,
                    int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setForeground(UITheme.SUCCESS);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.BG_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        // ── Action Panel ──
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(0, 48));

        JLabel lblAction = new JLabel("Tindakan Evakuasi:");
        lblAction.setFont(UITheme.FONT_BODY);
        lblAction.setForeground(UITheme.TEXT_SECONDARY);

        RoundedButton btnEvacCritical = new RoundedButton("Evakuasi Kritis", UITheme.DANGER);
        btnEvacCritical.setPreferredSize(new Dimension(140, 36));
        btnEvacCritical.addActionListener(e -> evacuateCritical());

        RoundedButton btnEvacInjured = new RoundedButton("Evakuasi Luka", UITheme.ACCENT_ORANGE);
        btnEvacInjured.setPreferredSize(new Dimension(140, 36));
        btnEvacInjured.addActionListener(e -> evacuateInjured());

        RoundedButton btnSafe = new RoundedButton("Tandai Aman", UITheme.INFO);
        btnSafe.setPreferredSize(new Dimension(130, 36));
        btnSafe.addActionListener(e -> markSafe());

        RoundedButton btnEvacAll = new RoundedButton("Amankan Semua", UITheme.SUCCESS);
        btnEvacAll.setPreferredSize(new Dimension(150, 36));
        btnEvacAll.addActionListener(e -> evacuateAll());

        actionPanel.add(lblAction);
        actionPanel.add(btnEvacCritical);
        actionPanel.add(btnEvacInjured);
        actionPanel.add(btnSafe);
        actionPanel.add(btnEvacAll);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private JLabel createSummaryCard(JPanel parent, String label, String value, Color color) {
        RoundedPanel card = new RoundedPanel(UITheme.BG_CARD, 12);
        card.setHasBorder(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblVal.setForeground(color);
        lblVal.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblName = new JLabel(label);
        lblName.setFont(UITheme.FONT_SMALL);
        lblName.setForeground(UITheme.TEXT_SECONDARY);
        lblName.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblVal);
        card.add(Box.createVerticalStrut(2));
        card.add(lblName);

        parent.add(card);
        return lblVal;
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(2000, e -> refresh());
        autoRefreshTimer.start();
    }

    public void refresh() {
        int selectedRow = table.getSelectedRow();
        activeIncidents.clear();
        tableModel.setRowCount(0);

        int total = 0, critical = 0, injured = 0, evacuated = 0, safe = 0;

        ArrayList<Incident> active = IncidentService.getActiveIncidents();
        for (Incident inc : active) {
            activeIncidents.add(inc);
            tableModel.addRow(new Object[] {
                    inc.getIncidentId(),
                    inc.getLocation().replaceAll("\\[.*?\\]", "").trim(),
                    inc.getNumVictimsTrapped(),
                    inc.getVictimsCritical(),
                    inc.getVictimsInjured(),
                    inc.getVictimsEvacuated(),
                    inc.getVictimsSafe()
            });
            total += inc.getNumVictimsTrapped();
            critical += inc.getVictimsCritical();
            injured += inc.getVictimsInjured();
            evacuated += inc.getVictimsEvacuated();
            safe += inc.getVictimsSafe();
        }

        lblTotal.setText(String.valueOf(total));
        lblCritical.setText(String.valueOf(critical));
        lblInjured.setText(String.valueOf(injured));
        lblEvacuated.setText(String.valueOf(evacuated));
        lblSafe.setText(String.valueOf(safe));

        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount())
            table.setRowSelectionInterval(selectedRow, selectedRow);
    }

    private Incident getSelectedIncident() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= activeIncidents.size())
            return null;
        return activeIncidents.get(row);
    }

    private void evacuateCritical() {
        Incident inc = getSelectedIncident();
        if (inc == null) {
            warn("Pilih insiden terlebih dahulu.");
            return;
        }
        if (inc.getVictimsCritical() > 0) {
            inc.setVictimsCritical(inc.getVictimsCritical() - 1);
            inc.setVictimsEvacuated(inc.getVictimsEvacuated() + 1);
            refresh();
        } else {
            warn("Tidak ada korban kritis pada insiden ini.");
        }
    }

    private void evacuateInjured() {
        Incident inc = getSelectedIncident();
        if (inc == null) {
            warn("Pilih insiden terlebih dahulu.");
            return;
        }
        if (inc.getVictimsInjured() > 0) {
            inc.setVictimsInjured(inc.getVictimsInjured() - 1);
            inc.setVictimsEvacuated(inc.getVictimsEvacuated() + 1);
            refresh();
        } else {
            warn("Tidak ada korban luka-luka pada insiden ini.");
        }
    }

    private void markSafe() {
        Incident inc = getSelectedIncident();
        if (inc == null) {
            warn("Pilih insiden terlebih dahulu.");
            return;
        }
        if (inc.getVictimsEvacuated() > 0) {
            inc.setVictimsEvacuated(inc.getVictimsEvacuated() - 1);
            inc.setVictimsSafe(inc.getVictimsSafe() + 1);
            refresh();
        } else {
            warn("Tidak ada korban dievakuasi pada insiden ini yang siap diamankan.");
        }
    }

    private void evacuateAll() {
        Incident inc = getSelectedIncident();
        if (inc == null) {
            warn("Pilih insiden terlebih dahulu.");
            return;
        }
        int moving = inc.getVictimsCritical() + inc.getVictimsInjured() + inc.getVictimsEvacuated();
        if (moving > 0) {
            inc.setVictimsSafe(inc.getVictimsSafe() + moving);
            inc.setVictimsCritical(0);
            inc.setVictimsInjured(0);
            inc.setVictimsEvacuated(0);
            JOptionPane.showMessageDialog(this, "Semua korban berhasil dievakuasi dan diamankan!", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else {
            info("Semua korban sudah aman.");
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
