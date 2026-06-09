package ui.admin;

import database.Database;
import model.*;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FiretruckResourcePanel extends JPanel {

    private JPanel cardsContainer;
    private JPanel editOverlay;
    private JScrollPane editScroll;
    private Firetruck editingTruck;

    // UI labels and buttons in details/actions side card
    private JLabel lblEditTitle;
    private JLabel lblEditPlate;
    private JLabel lblEditType;
    private JLabel lblStatusInfo;

    private JLabel lblWaterVal;
    private JLabel lblFuelVal;

    private JLabel lblFoamVal, lblHoseVal, lblLadderVal, lblOxygenVal, lblFirstAidVal;

    private RoundedButton btnRefill;
    private RoundedButton btnToggleMaintenance;

    public FiretruckResourcePanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header ──
        JLabel title = new JLabel("  Kelola Sumber Daya Kendaraan");
        title.setIcon(new VectorIcon(VectorIcon.Type.TRUCK, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel(
                "Pantau kondisi logistik kendaraan. Kirim kendaraan kosong kembali ke pos untuk isi ulang.");
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

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(headerLeft, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        // ── Cards container ──
        cardsContainer = new JPanel();
        cardsContainer.setOpaque(false);
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        // ── Actions overlay ──
        editOverlay = buildEditOverlay();
        editScroll = new JScrollPane(editOverlay);
        editScroll.setOpaque(false);
        editScroll.getViewport().setOpaque(false);
        editScroll.setBorder(null);
        editScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        editScroll.getVerticalScrollBar().setUnitIncrement(16);
        editScroll.setVisible(false);

        // Use layered approach
        JPanel mainArea = new JPanel(new BorderLayout(16, 0));
        mainArea.setOpaque(false);
        mainArea.add(scroll, BorderLayout.CENTER);
        mainArea.add(editScroll, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(mainArea, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        cardsContainer.removeAll();
        ArrayList<Firetruck> trucks = Database.getFireStation().getFiretrucks();

        for (Firetruck truck : trucks) {
            cardsContainer.add(buildTruckCard(truck));
            cardsContainer.add(Box.createVerticalStrut(12));
        }

        if (editingTruck != null) {
            openEdit(editingTruck); // refresh details card
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel buildTruckCard(Firetruck truck) {
        RoundedPanel card = new RoundedPanel(UITheme.BG_CARD, 16);
        card.setHasBorder(true);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // TOP ROW
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        // Left: ID + Status
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        JLabel lblId = new JLabel(truck.getId());
        lblId.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 20));
        lblId.setForeground(UITheme.TEXT_PRIMARY);
        lblId.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblType = new JLabel(truck.getType().getLabel());
        lblType.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        lblType.setForeground(UITheme.ACCENT);
        lblType.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblPlate = new JLabel(truck.getPlateNumber());
        lblPlate.setFont(UITheme.FONT_SMALL);
        lblPlate.setForeground(UITheme.TEXT_SECONDARY);
        lblPlate.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusBadge = new JLabel(truck.getStatus().getLabel());
        statusBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        statusBadge.setAlignmentX(LEFT_ALIGNMENT);
        Color statusColor = getStatusColor(truck.getStatus());
        statusBadge.setForeground(statusColor);
        statusBadge.setBackground(new Color(
                Math.min(statusColor.getRed() / 4, 80),
                Math.min(statusColor.getGreen() / 4, 80),
                Math.min(statusColor.getBlue() / 4, 80)));

        leftPanel.add(lblId);
        leftPanel.add(lblType);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(lblPlate);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(statusBadge);

        // Right: Resources + Actions button
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BorderLayout(0, 8));

        JLabel lblResources = new JLabel("Perlengkapan:");
        lblResources.setFont(UITheme.FONT_SMALL);
        lblResources.setForeground(UITheme.TEXT_MUTED);
        lblResources.setAlignmentX(LEFT_ALIGNMENT);

        StringBuilder resText = new StringBuilder("<html>");
        HashMap<Resource, Integer> res = truck.getResources();
        for (Resource r : Resource.values()) {
            if (r == Resource.WATER)
                continue;
            int qty = res.getOrDefault(r, 0);
            resText.append(r.getDisplayName()).append(": <b>").append(qty).append("</b>  ");
        }
        resText.append("</html>");

        JLabel lblResDetail = new JLabel(resText.toString());
        lblResDetail.setFont(UITheme.FONT_SMALL);
        lblResDetail.setForeground(UITheme.TEXT_SECONDARY);
        lblResDetail.setAlignmentX(LEFT_ALIGNMENT);

        StringBuilder crewText = new StringBuilder("<html>Kru: ");
        if (truck.getCrew().isEmpty()) {
            crewText.append("<i>Belum ditugaskan</i>");
        } else {
            for (int i = 0; i < truck.getCrew().size(); i++) {
                if (i > 0)
                    crewText.append(", ");
                crewText.append(truck.getCrew().get(i).getName());
            }
        }
        crewText.append("</html>");
        JLabel lblCrew = new JLabel(crewText.toString());
        lblCrew.setFont(UITheme.FONT_SMALL);
        lblCrew.setForeground(UITheme.TEXT_MUTED);
        lblCrew.setAlignmentX(LEFT_ALIGNMENT);

        RoundedButton btnEdit = new RoundedButton("  Tindakan", UITheme.ACCENT_ORANGE);
        btnEdit.setIcon(new VectorIcon(VectorIcon.Type.WRENCH, 14, Color.WHITE));
        btnEdit.setPreferredSize(new Dimension(85, 32));
        btnEdit.addActionListener(e -> openEdit(truck));

        JPanel resourcesBox = new JPanel();
        resourcesBox.setOpaque(false);
        resourcesBox.setLayout(new BoxLayout(resourcesBox, BoxLayout.Y_AXIS));
        resourcesBox.add(lblResources);
        resourcesBox.add(Box.createVerticalStrut(2));
        resourcesBox.add(lblResDetail);
        resourcesBox.add(Box.createVerticalStrut(4));
        resourcesBox.add(lblCrew);

        rightPanel.add(resourcesBox, BorderLayout.CENTER);
        rightPanel.add(btnEdit, BorderLayout.SOUTH);

        topRow.add(leftPanel, BorderLayout.WEST);
        topRow.add(rightPanel, BorderLayout.CENTER);

        // BOTTOM ROW: level bars spanning full width
        JPanel barsRow = new JPanel();
        barsRow.setOpaque(false);
        barsRow.setLayout(new BoxLayout(barsRow, BoxLayout.Y_AXIS));
        barsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        barsRow.add(buildLevelBar("Air", truck.getCurrentWater(), truck.getWaterCapacity(),
                truck.getCurrentWater() < 1000 ? UITheme.DANGER : UITheme.INFO, "L"));
        barsRow.add(Box.createVerticalStrut(6));
        barsRow.add(buildLevelBar("BBM", truck.getFuelLevel(), 100,
                truck.getFuelLevel() < 30 ? UITheme.DANGER : UITheme.SUCCESS, "%"));

        card.add(topRow, BorderLayout.CENTER);
        card.add(barsRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildLevelBar(String label, int current, int max, Color barColor, String unit) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(30, 20));

        double pct = (max > 0) ? (double) current / max : 0;
        String text = current + " / " + max + " " + unit;

        JProgressBar bar = new JProgressBar(0, max) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), r = h / 2;

                // track
                g2.setColor(UITheme.BG_DARK);
                g2.fillRoundRect(0, 0, w, h, r, r);

                // filled portion
                int filled = (int) Math.round(w * ((double) getValue() / getMaximum()));
                if (filled > 0) {
                    g2.setColor(getForeground());
                    g2.fillRoundRect(0, 0, filled, h, r, r);
                }

                // text
                if (isStringPainted()) {
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    String s = getString();
                    int tx = (w - fm.stringWidth(s)) / 2;
                    int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                    g2.setFont(getFont());
                    g2.setColor(Color.WHITE);
                    g2.drawString(s, tx, ty);
                }
                g2.dispose();
            }
        };
        bar.setValue(current);
        bar.setStringPainted(true);
        bar.setString(text);
        bar.setFont(UITheme.FONT_SMALL);
        bar.setForeground(barColor);
        bar.setBackground(UITheme.BG_DARK);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder());

        panel.add(lbl, BorderLayout.WEST);
        panel.add(bar, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEditOverlay() {
        RoundedPanel panel = new RoundedPanel(UITheme.BG_CARD, 16);
        panel.setHasBorder(true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(280, 560));

        lblEditTitle = new JLabel("Tindakan Kendaraan");
        lblEditTitle.setIcon(new VectorIcon(VectorIcon.Type.WRENCH, 18, UITheme.ACCENT_ORANGE));
        lblEditTitle.setFont(UITheme.FONT_SUB);
        lblEditTitle.setForeground(UITheme.ACCENT_ORANGE);
        lblEditTitle.setAlignmentX(LEFT_ALIGNMENT);

        lblEditPlate = new JLabel("--");
        lblEditPlate.setFont(UITheme.FONT_SMALL);
        lblEditPlate.setForeground(UITheme.TEXT_SECONDARY);
        lblEditPlate.setAlignmentX(LEFT_ALIGNMENT);

        lblEditType = new JLabel("--");
        lblEditType.setFont(UITheme.FONT_SMALL);
        lblEditType.setForeground(UITheme.TEXT_SECONDARY);
        lblEditType.setAlignmentX(LEFT_ALIGNMENT);

        lblStatusInfo = new JLabel("Status: Standby");
        lblStatusInfo.setFont(UITheme.FONT_BODY);
        lblStatusInfo.setForeground(UITheme.SUCCESS);
        lblStatusInfo.setAlignmentX(LEFT_ALIGNMENT);

        // Current Resource details
        lblWaterVal = metricLabel("Air", "0 / 5000 L");
        lblFuelVal = metricLabel("BBM", "0%");

        lblFoamVal = metricLabel("Busa (Foam)", "0 L");
        lblHoseVal = metricLabel("Selang (Hose)", "0");
        lblLadderVal = metricLabel("Tangga (Ladder)", "0");
        lblOxygenVal = metricLabel("Oksigen", "0");
        lblFirstAidVal = metricLabel("P3K", "0");

        btnRefill = new RoundedButton("  Isi Ulang Air & BBM", UITheme.INFO);
        btnRefill.setIcon(new VectorIcon(VectorIcon.Type.REFRESH, 14, Color.WHITE));
        btnRefill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRefill.setAlignmentX(LEFT_ALIGNMENT);
        btnRefill.addActionListener(e -> refillResources());

        btnToggleMaintenance = new RoundedButton("  Kirim ke Perawatan", UITheme.DANGER);
        btnToggleMaintenance.setIcon(new VectorIcon(VectorIcon.Type.ALERT, 14, Color.WHITE));
        btnToggleMaintenance.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnToggleMaintenance.setAlignmentX(LEFT_ALIGNMENT);
        btnToggleMaintenance.addActionListener(e -> toggleMaintenanceStatus());

        RoundedButton btnClose = new RoundedButton("  Tutup", UITheme.BG_CARD);
        btnClose.setIcon(new VectorIcon(VectorIcon.Type.BACK, 14, UITheme.TEXT_PRIMARY));
        btnClose.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnClose.setAlignmentX(LEFT_ALIGNMENT);
        btnClose.addActionListener(e -> {
            editingTruck = null;
            editScroll.setVisible(false);
        });

        panel.add(lblEditTitle);
        panel.add(lblEditPlate);
        panel.add(lblEditType);
        panel.add(Box.createVerticalStrut(12));

        panel.add(lblStatusInfo);
        panel.add(Box.createVerticalStrut(12));

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(UITheme.BORDER);
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep1);
        panel.add(Box.createVerticalStrut(10));

        panel.add(editLabel("Data Logistik & Kapasitas:"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblWaterVal);
        panel.add(lblFuelVal);
        panel.add(lblFoamVal);
        panel.add(lblHoseVal);
        panel.add(lblLadderVal);
        panel.add(lblOxygenVal);
        panel.add(lblFirstAidVal);

        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(16));
        panel.add(btnRefill);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnToggleMaintenance);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnClose);

        return panel;
    }

    private JLabel metricLabel(String name, String initialVal) {
        JLabel l = new JLabel("• " + name + ": " + initialVal);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel editLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SUB);
        l.setForeground(UITheme.ACCENT_ORANGE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void openEdit(Firetruck truck) {
        editingTruck = truck;
        lblEditTitle.setText("  Detail: " + truck.getId());
        lblEditPlate.setText("Plate: " + truck.getPlateNumber());
        lblEditType.setText("Tipe: " + truck.getType().getLabel());

        lblWaterVal.setText("• Air: " + truck.getCurrentWater() + " / " + truck.getWaterCapacity() + " L");
        lblFuelVal.setText("• BBM: " + truck.getFuelLevel() + "%");

        HashMap<Resource, Integer> res = truck.getResources();
        lblFoamVal.setText("• Busa: " + res.getOrDefault(Resource.FOAM, 200) + " L");
        lblHoseVal.setText("• Selang: " + res.getOrDefault(Resource.HOSE, 4) + " unit");
        lblLadderVal.setText("• Tangga: " + res.getOrDefault(Resource.LADDER, 2) + " unit");
        lblOxygenVal.setText("• Tabung Oksigen: " + res.getOrDefault(Resource.OXYGEN_TANK, 6) + " unit");
        lblFirstAidVal.setText("• Kotak P3K: " + res.getOrDefault(Resource.FIRST_AID_KIT, 3) + " unit");

        boolean deployed = truck.getStatus() == TruckStatus.DEPLOYED;
        boolean inMaintenance = truck.getStatus() == TruckStatus.MAINTENANCE;

        if (deployed) {
            lblStatusInfo.setText("Status: SEDANG BERTUGAS");
            lblStatusInfo.setForeground(UITheme.ACCENT_ORANGE);
            btnRefill.setEnabled(false);
            btnToggleMaintenance.setEnabled(false);
            btnToggleMaintenance.setText("  Sedang Bertugas");
        } else if (inMaintenance) {
            lblStatusInfo.setText("Status: DALAM PERAWATAN");
            lblStatusInfo.setForeground(UITheme.DANGER);
            btnRefill.setEnabled(true); // Can refill while in depot
            btnToggleMaintenance.setEnabled(true);
            btnToggleMaintenance.setText("  Selesai Perawatan");
            btnToggleMaintenance.setBaseColor(UITheme.SUCCESS);
        } else {
            lblStatusInfo.setText("Status: STANDBY DI POS");
            lblStatusInfo.setForeground(UITheme.SUCCESS);
            btnRefill.setEnabled(true);
            btnToggleMaintenance.setEnabled(true);
            btnToggleMaintenance.setText("  Kirim ke Perawatan");
            btnToggleMaintenance.setBaseColor(UITheme.DANGER);
        }

        editScroll.setVisible(true);
    }

    private void refillResources() {
        if (editingTruck == null)
            return;
        editingTruck.setCurrentWater(editingTruck.getWaterCapacity());
        editingTruck.setFuelLevel(100);
        editingTruck.setResource(Resource.FOAM, 200);
        editingTruck.setResource(Resource.HOSE, 4);
        editingTruck.setResource(Resource.LADDER, 2);
        editingTruck.setResource(Resource.OXYGEN_TANK, 6);
        editingTruck.setResource(Resource.FIRST_AID_KIT, 3);

        JOptionPane.showMessageDialog(this,
                "<html><b>" + editingTruck.getId() + " diisi ulang sepenuhnya!</b><br>"
                        + "Kapasitas Air dan BBM kembali ke 100%. Logistik dipulihkan.</html>",
                "Isi Ulang Berhasil", JOptionPane.INFORMATION_MESSAGE);

        refresh();
    }

    private void toggleMaintenanceStatus() {
        if (editingTruck == null)
            return;
        if (editingTruck.getStatus() == TruckStatus.MAINTENANCE) {
            editingTruck.setStatus(TruckStatus.AVAILABLE);
            JOptionPane.showMessageDialog(this, editingTruck.getId() + " siap bertugas kembali di pos.",
                    "Perawatan Selesai", JOptionPane.INFORMATION_MESSAGE);
        } else if (editingTruck.getStatus() == TruckStatus.AVAILABLE) {
            editingTruck.setStatus(TruckStatus.MAINTENANCE);
            JOptionPane.showMessageDialog(this, editingTruck.getId() + " dikirim ke bengkel perawatan.",
                    "Masuk Perawatan", JOptionPane.INFORMATION_MESSAGE);
        }
        refresh();
    }

    private Color getStatusColor(TruckStatus status) {
        switch (status) {
            case AVAILABLE:
                return UITheme.SUCCESS;
            case DEPLOYED:
                return UITheme.ACCENT_ORANGE;
            case MAINTENANCE:
                return UITheme.DANGER;
            default:
                return UITheme.TEXT_SECONDARY;
        }
    }
}

// Trigger recompile
