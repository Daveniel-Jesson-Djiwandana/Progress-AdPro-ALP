package ui.admin;

import database.Database;
import service.AuthService;
import ui.MainFrame;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.VectorIcon;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JPanel {

    public static final String CARD_AUTO       = "AUTO_FIRE";
    public static final String CARD_DISPATCH   = "DISPATCH";
    public static final String CARD_STATUS     = "STATUS";
    public static final String CARD_HISTORY    = "HISTORY";
    public static final String CARD_PRIORITY   = "PRIORITY";
    public static final String CARD_CIVILIANS  = "CIVILIANS";
    public static final String CARD_RESOURCES  = "RESOURCES";

    private final MainFrame frame;
    private JLabel lblUsername;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentPanel  = new JPanel(contentLayout);

    private AutoFirePanel          autoFirePanel;
    private DispatchPanel          dispatchPanel;
    private AdminStatusPanel       statusPanel;
    private AdminHistoryPanel      historyPanel;
    private PriorityRulesPanel     priorityRulesPanel;
    private CivilianMonitorPanel   civilianMonitorPanel;
    private FiretruckResourcePanel firetruckResourcePanel;

    public AdminDashboard(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        add(buildSidebar(), BorderLayout.WEST);

        autoFirePanel  = new AutoFirePanel(() -> {
            if (statusPanel   != null) statusPanel.refresh();
            if (dispatchPanel != null) dispatchPanel.refresh();
        });
        dispatchPanel          = new DispatchPanel();
        statusPanel            = new AdminStatusPanel();
        historyPanel           = new AdminHistoryPanel();
        priorityRulesPanel     = new PriorityRulesPanel();
        civilianMonitorPanel   = new CivilianMonitorPanel();
        firetruckResourcePanel = new FiretruckResourcePanel();

        contentPanel.setBackground(UITheme.BG_DARK);
        contentPanel.add(autoFirePanel,          CARD_AUTO);
        contentPanel.add(dispatchPanel,          CARD_DISPATCH);
        contentPanel.add(statusPanel,            CARD_STATUS);
        contentPanel.add(historyPanel,           CARD_HISTORY);
        contentPanel.add(priorityRulesPanel,     CARD_PRIORITY);
        contentPanel.add(civilianMonitorPanel,   CARD_CIVILIANS);
        contentPanel.add(firetruckResourcePanel, CARD_RESOURCES);

        add(contentPanel, BorderLayout.CENTER);
        showContent(CARD_AUTO);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(238, Integer.MAX_VALUE));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Header
        JPanel header = new JPanel();
        header.setBackground(UITheme.BG_DARK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel fire = new JLabel();
        fire.setIcon(new VectorIcon(VectorIcon.Type.FIRE, 32, UITheme.ACCENT_ORANGE));
        fire.setAlignmentX(LEFT_ALIGNMENT);

        JLabel appTitle = new JLabel("Panel Admin");
        appTitle.setFont(UITheme.FONT_SUB);
        appTitle.setForeground(UITheme.ACCENT_ORANGE);
        appTitle.setAlignmentX(LEFT_ALIGNMENT);

        lblUsername = new JLabel("Petugas");
        lblUsername.setFont(UITheme.FONT_SMALL);
        lblUsername.setForeground(UITheme.TEXT_MUTED);
        lblUsername.setAlignmentX(LEFT_ALIGNMENT);

        header.add(fire);
        header.add(Box.createVerticalStrut(4));
        header.add(appTitle);
        header.add(Box.createVerticalStrut(1));
        header.add(lblUsername);

        JLabel sSim    = sectionLabel("SIMULASI KEBAKARAN");
        RoundedButton bAuto     = navBtn("Sistem Auto Fire",   VectorIcon.Type.FIRE,    CARD_AUTO);

        JLabel sOps    = sectionLabel("OPERASIONAL");
        RoundedButton bDispatch = navBtn("Dispatch Kendaraan", VectorIcon.Type.TRUCK,   CARD_DISPATCH);

        JLabel sConfig = sectionLabel("KONFIGURASI");
        RoundedButton bPriority = navBtn("Aturan Prioritas",   VectorIcon.Type.SETTINGS, CARD_PRIORITY);

        JLabel sMonitor = sectionLabel("PEMANTAUAN");
        RoundedButton bCivilians = navBtn("Monitor Korban",    VectorIcon.Type.PEOPLE,   CARD_CIVILIANS);

        JLabel sResources = sectionLabel("SUMBER DAYA");
        RoundedButton bResources = navBtn("Logistik Armada",   VectorIcon.Type.WRENCH,   CARD_RESOURCES);

        JLabel sData   = sectionLabel("DATA & STATUS");
        RoundedButton bStatus   = navBtn("Status Insiden",     VectorIcon.Type.STATUS,  CARD_STATUS);
        RoundedButton bHistory  = navBtn("Riwayat Laporan",    VectorIcon.Type.HISTORY, CARD_HISTORY);

        RoundedButton btnLogout = new RoundedButton("Keluar", UITheme.BG_CARD);
        btnLogout.setIcon(new VectorIcon(VectorIcon.Type.LOGOUT, 16, UITheme.TEXT_PRIMARY));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> { AuthService.logout(); frame.onLogout(); });

        sidebar.add(header);
        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sSim);
        sidebar.add(bAuto);
        sidebar.add(Box.createVerticalStrut(8));
        
        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sOps);
        sidebar.add(bDispatch);
        sidebar.add(Box.createVerticalStrut(8));

        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sConfig);
        sidebar.add(bPriority);
        sidebar.add(Box.createVerticalStrut(8));

        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sMonitor);
        sidebar.add(bCivilians);
        sidebar.add(Box.createVerticalStrut(8));

        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sResources);
        sidebar.add(bResources);
        sidebar.add(Box.createVerticalStrut(8));
        
        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sData);
        sidebar.add(bStatus);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(bHistory);
        sidebar.add(Box.createVerticalGlue());
        addDiv(sidebar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(16));

        return sidebar;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(4, 16, 2, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private RoundedButton navBtn(String text, VectorIcon.Type iconType, String card) {
        RoundedButton btn = new RoundedButton("  " + text, UITheme.BG_SIDEBAR);
        btn.setIcon(new VectorIcon(iconType, 16, UITheme.TEXT_PRIMARY));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        btn.addActionListener(e -> showContent(card));
        return btn;
    }

    private void addDiv(JPanel p) {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setBackground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.add(sep);
    }

    public void showContent(String card) {
        contentLayout.show(contentPanel, card);
        switch (card) {
            case CARD_DISPATCH:  dispatchPanel.refresh(); break;
            case CARD_STATUS:    statusPanel.refresh();   break;
            case CARD_HISTORY:   historyPanel.refresh();  break;
            case CARD_PRIORITY:  priorityRulesPanel.refresh(); break;
            case CARD_CIVILIANS: civilianMonitorPanel.refresh(); break;
            case CARD_RESOURCES: firetruckResourcePanel.refresh(); break;
        }
    }

    public void refresh() {
        if (Database.getCurrentUser() != null) {
            lblUsername.setText(Database.getCurrentUser().getName());
        }
    }
}
