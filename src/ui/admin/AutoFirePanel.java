package ui.admin;

import model.Incident;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AutoFirePanel — kebakaran muncul secara otomatis via Swing Timer.
 * Admin bisa ON/OFF dan mengatur interval. Manual trigger juga tersedia.
 */
public class AutoFirePanel extends JPanel {

    private static final int DEFAULT_INTERVAL_SEC = 20;

    private Timer  countdownTimer;
    private Timer  fireTimer;
    private int    intervalSec   = DEFAULT_INTERVAL_SEC;
    private int    countdown     = DEFAULT_INTERVAL_SEC;
    private boolean running      = false;

    private JLabel        lblCountdown;
    private JLabel        lblStatus;
    private JSpinner      spInterval;
    private RoundedButton btnToggle;
    private DefaultListModel<String> logModel;
    private JList<String> logList;

    // Callback to notify sibling panels (e.g. map, status)
    private Runnable onNewIncident;

    public AutoFirePanel(Runnable onNewIncident) {
        this.onNewIncident = onNewIncident;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        buildUI();
        buildTimers();
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        // Header
        JLabel title = new JLabel("  Sistem Kebakaran Otomatis");
        title.setIcon(new VectorIcon(VectorIcon.Type.FIRE, 24, UITheme.ACCENT_ORANGE));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Insiden kebakaran muncul secara acak dan otomatis tanpa intervensi admin.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(sub);

        // Centre: countdown card + controls
        JPanel centre = new JPanel(new GridLayout(1, 2, 20, 0));
        centre.setOpaque(false);
        centre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Countdown card
        RoundedPanel countdownCard = new RoundedPanel(UITheme.BG_CARD, 20);
        countdownCard.setHasBorder(true);
        countdownCard.setLayout(new GridBagLayout());

        JPanel countdownInner = new JPanel();
        countdownInner.setOpaque(false);
        countdownInner.setLayout(new BoxLayout(countdownInner, BoxLayout.Y_AXIS));

        lblStatus = new JLabel("● Sistem MATI", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SUB);
        lblStatus.setForeground(UITheme.TEXT_MUTED);
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);

        lblCountdown = new JLabel("--", SwingConstants.CENTER);
        lblCountdown.setFont(new Font("SansSerif", Font.BOLD, 56));
        lblCountdown.setForeground(UITheme.ACCENT_ORANGE);
        lblCountdown.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSec = new JLabel("detik hingga insiden berikutnya", SwingConstants.CENTER);
        lblSec.setFont(UITheme.FONT_SMALL);
        lblSec.setForeground(UITheme.TEXT_SECONDARY);
        lblSec.setAlignmentX(CENTER_ALIGNMENT);

        countdownInner.add(lblStatus);
        countdownInner.add(Box.createVerticalStrut(6));
        countdownInner.add(lblCountdown);
        countdownInner.add(Box.createVerticalStrut(2));
        countdownInner.add(lblSec);
        countdownCard.add(countdownInner);

        // Controls card
        RoundedPanel controlCard = new RoundedPanel(UITheme.BG_CARD, 20);
        controlCard.setHasBorder(true);
        controlCard.setLayout(new BoxLayout(controlCard, BoxLayout.Y_AXIS));
        controlCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel lInterval = new JLabel("Interval (detik):");
        lInterval.setFont(UITheme.FONT_BODY);
        lInterval.setForeground(UITheme.TEXT_SECONDARY);
        lInterval.setAlignmentX(LEFT_ALIGNMENT);

        spInterval = new JSpinner(new SpinnerNumberModel(DEFAULT_INTERVAL_SEC, 5, 120, 5));
        spInterval.setFont(UITheme.FONT_BODY);
        spInterval.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        spInterval.setAlignmentX(LEFT_ALIGNMENT);
        spInterval.addChangeListener(e -> {
            intervalSec = (int) spInterval.getValue();
            if (!running) countdown = intervalSec;
        });

        btnToggle = new RoundedButton("  Aktifkan Sistem", UITheme.SUCCESS);
        btnToggle.setIcon(new VectorIcon(VectorIcon.Type.BOLT, 16, Color.WHITE));
        btnToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnToggle.setAlignmentX(LEFT_ALIGNMENT);
        btnToggle.addActionListener(e -> toggleSystem());

        RoundedButton btnManual = new RoundedButton("  Acak Manual Sekarang", UITheme.ACCENT_ORANGE);
        btnManual.setIcon(new VectorIcon(VectorIcon.Type.DICE, 16, Color.WHITE));
        btnManual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnManual.setAlignmentX(LEFT_ALIGNMENT);
        btnManual.addActionListener(e -> triggerFire("Manual Admin"));

        controlCard.add(lInterval);
        controlCard.add(Box.createVerticalStrut(6));
        controlCard.add(spInterval);
        controlCard.add(Box.createVerticalStrut(14));
        controlCard.add(btnToggle);
        controlCard.add(Box.createVerticalStrut(10));
        controlCard.add(btnManual);

        centre.add(countdownCard);
        centre.add(controlCard);

        // Log list
        logModel = new DefaultListModel<>();
        logList  = new JList<>(logModel);
        logList.setFont(UITheme.FONT_SMALL);
        logList.setBackground(UITheme.BG_SURFACE);
        logList.setForeground(UITheme.TEXT_PRIMARY);
        logList.setFixedCellHeight(22);

        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER), "Log Insiden Otomatis",
            0, 0, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY));
        logScroll.getViewport().setBackground(UITheme.BG_SURFACE);

        add(titlePanel, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);
    }

    // ── Timer Logic ───────────────────────────────────────────────────────────

    private void buildTimers() {
        // Countdown timer: tick every second
        countdownTimer = new Timer(1000, e -> {
            if (!running) return;
            countdown--;
            lblCountdown.setText(String.valueOf(countdown));
            if (countdown <= 0) {
                triggerFire("Sistem Otomatis");
                countdown = intervalSec;
            }
        });

        // Progress timer: advance dispatched incident progress every 3s
        fireTimer = new Timer(3000, e -> advanceAllProgress());
        fireTimer.start(); // always running
    }

    private void toggleSystem() {
        running = !running;
        if (running) {
            countdown = intervalSec;
            lblCountdown.setText(String.valueOf(countdown));
            lblStatus.setText("● Sistem AKTIF");
            lblStatus.setForeground(UITheme.SUCCESS);
            btnToggle.setBaseColor(UITheme.DANGER);
            btnToggle.setText("  Matikan Sistem");
            btnToggle.setIcon(new VectorIcon(VectorIcon.Type.LOGOUT, 16, Color.WHITE));
            spInterval.setEnabled(false);
            countdownTimer.start();
        } else {
            countdownTimer.stop();
            lblCountdown.setText("--");
            lblStatus.setText("● Sistem MATI");
            lblStatus.setForeground(UITheme.TEXT_MUTED);
            btnToggle.setBaseColor(UITheme.SUCCESS);
            btnToggle.setText("  Aktifkan Sistem");
            btnToggle.setIcon(new VectorIcon(VectorIcon.Type.BOLT, 16, Color.WHITE));
            spInterval.setEnabled(true);
        }
    }

    private void triggerFire(String label) {
        Incident inc = IncidentService.randomizeIncident(label + " (Simulasi)");
        String time  = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String entry = String.format("[%s] %s — %s [%s]",
            time, inc.getIncidentId(), inc.getLocation(), inc.getSeverity().getLabel());
        logModel.add(0, entry); // prepend (newest first)
        if (logModel.size() > 50) logModel.remove(logModel.size() - 1);
        if (onNewIncident != null) onNewIncident.run();
    }

    private void advanceAllProgress() {
        for (Incident inc : service.IncidentService.getActiveIncidents()) {
            if (inc.getStatus() == model.IncidentStatus.DISPATCHED && inc.getTrucksAssigned() > 0) {
                int step = Math.max(1, inc.getTrucksAssigned() * 2);
                inc.advanceProgress(step);
            }
        }
    }
}
