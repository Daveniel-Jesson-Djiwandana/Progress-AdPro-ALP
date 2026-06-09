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
import java.util.ArrayList;
import java.util.Date;


public class AutoFirePanel extends JPanel {

    private static final int DEFAULT_INTERVAL_SEC = 20;

    private Timer countdownTimer;
    private Timer fireTimer;
    private int intervalSec = DEFAULT_INTERVAL_SEC;
    private int countdown = DEFAULT_INTERVAL_SEC;
    private boolean running = false;

    private JLabel lblCountdown;
    private JLabel lblStatus;
    private JSpinner spInterval;
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

        lblStatus = new JLabel("Sistem MATI", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SUB);
        lblStatus.setForeground(UITheme.TEXT_MUTED);
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);

        lblCountdown = new JLabel("--", SwingConstants.CENTER);
        lblCountdown.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 56));
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
            if (!running)
                countdown = intervalSec;
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
        logList = new JList<>(logModel);
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

    // Timer Logic

    private void buildTimers() {
        // Countdown timer: tick every second
        countdownTimer = new Timer(1000, e -> {
            if (!running)
                return;
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
            lblStatus.setText("Sistem AKTIF");
            lblStatus.setForeground(UITheme.SUCCESS);
            btnToggle.setBaseColor(UITheme.DANGER);
            btnToggle.setText("  Matikan Sistem");
            btnToggle.setIcon(new VectorIcon(VectorIcon.Type.LOGOUT, 16, Color.WHITE));
            spInterval.setEnabled(false);
            countdownTimer.start();
        } else {
            countdownTimer.stop();
            lblCountdown.setText("--");
            lblStatus.setText("Sistem MATI");
            lblStatus.setForeground(UITheme.TEXT_MUTED);
            btnToggle.setBaseColor(UITheme.SUCCESS);
            btnToggle.setText("  Aktifkan Sistem");
            btnToggle.setIcon(new VectorIcon(VectorIcon.Type.BOLT, 16, Color.WHITE));
            spInterval.setEnabled(true);
        }
    }

    private void triggerFire(String label) {
        // Use a holder array so the lambda can capture a final reference,
        // then read the incident after randomizeIncident() returns.
        Incident[] holder = new Incident[1];
        holder[0] = IncidentService.randomizeIncident(label + " (Simulasi)", () -> {
            // Geocoding complete — update the log entry with the resolved address
            Incident inc = holder[0];
            if (inc == null)
                return;
            for (int i = 0; i < logModel.size(); i++) {
                String entry = logModel.get(i);
                if (entry.contains(inc.getIncidentId())) {
                    String t = entry.substring(1, 9); // HH:mm:ss
                    String newEntry = String.format("[%s] %s — %s [%s]",
                            t, inc.getIncidentId(), inc.getLocation(), inc.getSeverity().getLabel());
                    logModel.set(i, newEntry);
                    break;
                }
            }
            if (onNewIncident != null)
                onNewIncident.run();
        });
        Incident inc = holder[0];
        String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        String entry = String.format("[%s] %s — %s [%s]",
                time, inc.getIncidentId(), inc.getLocation(), inc.getSeverity().getLabel());
        logModel.add(0, entry); // prepend (newest first)
        if (logModel.size() > 50)
            logModel.remove(logModel.size() - 1);
        if (onNewIncident != null)
            onNewIncident.run();
    }

    private void advanceAllProgress() {
        boolean changed = false;

        // 1. Drain resources of deployed trucks
        ArrayList<model.Firetruck> stationTrucks = database.Database.getFireStation().getFiretrucks();

        // Count deployed supply trucks (Tipe 5 - Supply Air)
        int deployedSupplyTrucks = 0;
        for (model.Firetruck truck : stationTrucks) {
            if (truck.getStatus() == model.TruckStatus.DEPLOYED
                    && truck.getType() == model.FiretruckType.TYPE_5_WATER_SUPPLY) {
                deployedSupplyTrucks++;
            }
        }

        for (model.Firetruck truck : stationTrucks) {
            if (truck.getStatus() == model.TruckStatus.DEPLOYED) {
                // Deployed truck consumes water and fuel
                int waterDrain = 50; // liters per tick (lasts ~1.5 to 2.5 minutes)

                // Sistem Rotasi Air:
                // Tipe 5 (Supply Air) di belakang terus mencari sumber air & menyuplai unit
                // depan
                if (truck.getType() == model.FiretruckType.TYPE_5_WATER_SUPPLY) {
                    waterDrain = 0; // Supply air mencari air eksternal, airnya sendiri stabil/tidak terkuras habis
                } else if (deployedSupplyTrucks > 0) {
                    waterDrain = 10; // Konsumsi air unit depan melambat karena dibantu supply
                }

                int fuelDrain = 1; // 1 percent per tick (lasts ~5 minutes of continuous operation)

                int nextWater = Math.max(0, truck.getCurrentWater() - waterDrain);

                // Transfer air dari unit supply (Tipe 5) ke unit depan
                if (deployedSupplyTrucks > 0 && truck.getType() != model.FiretruckType.TYPE_5_WATER_SUPPLY) {
                    nextWater = Math.min(truck.getWaterCapacity(), nextWater + 30);
                }

                int nextFuel = Math.max(0, truck.getFuelLevel() - fuelDrain);

                truck.setCurrentWater(nextWater);
                truck.setFuelLevel(nextFuel);
                changed = true;

                // If either is empty, return to firestation (set status to AVAILABLE but
                // resource is 0)
                if (nextWater == 0 || nextFuel == 0) {
                    truck.setStatus(model.TruckStatus.AVAILABLE);
                    // Decrement assigned trucks of one active incident
                    for (Incident inc : service.IncidentService.getActiveIncidents()) {
                        if (inc.getStatus() == model.IncidentStatus.DISPATCHED && inc.getTrucksAssigned() > 0) {
                            inc.setTrucksAssigned(inc.getTrucksAssigned() - 1);
                            break;
                        }
                    }
                }
            }
        }

        // 2. Advance progress of incidents and auto-resolve
        for (Incident inc : service.IncidentService.getActiveIncidents()) {
            if (inc.getStatus() == model.IncidentStatus.DISPATCHED && inc.getTrucksAssigned() > 0) {
                if (inc.getDispatchStartTime() == null) {
                    inc.startDispatch();
                }
                long elapsed = java.time.Duration.between(inc.getDispatchStartTime(), java.time.LocalDateTime.now())
                        .getSeconds();

                int travelTime = 30; // seconds spent traveling to the scene
                int intensity = inc.getFireIntensity();
                int trucks = Math.max(1, inc.getTrucksAssigned());

                // Extinguishing time depends on fire intensity and number of trucks assigned
                double extinguishingTime = (intensity * 18.0) / trucks;

                int nextProgress = 0;
                if (elapsed <= travelTime) {
                    // Phase 1: En Route (0% to 30%)
                    nextProgress = (int) ((elapsed * 30.0) / travelTime);
                } else {
                    // Phase 2: On Scene / Extinguishing (30% to 100%)
                    double handlingElapsed = elapsed - travelTime;
                    nextProgress = 30 + (int) ((handlingElapsed * 70.0) / extinguishingTime);
                }

                // Cap progress at 100%
                nextProgress = Math.min(100, nextProgress);
                inc.setDispatchProgress(nextProgress);
                changed = true;

                if (nextProgress >= 100) {
                    // Auto-resolve!
                    int toFree = inc.getTrucksAssigned();
                    ArrayList<model.Firetruck> deployed = new ArrayList<>();
                    for (model.Firetruck t : stationTrucks) {
                        if (t.getStatus() == model.TruckStatus.DEPLOYED) {
                            deployed.add(t);
                        }
                    }
                    for (int i = 0; i < Math.min(toFree, deployed.size()); i++) {
                        deployed.get(i).setStatus(model.TruckStatus.AVAILABLE);
                    }
                    service.IncidentService.resolveIncident(inc, "Sistem Otomatis", toFree,
                            "Pemadaman otomatis selesai.");
                }
            }
        }

        if (changed && onNewIncident != null) {
            onNewIncident.run();
        }
    }
}
