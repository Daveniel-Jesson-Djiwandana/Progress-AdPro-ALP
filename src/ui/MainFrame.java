package ui;

import ui.screens.LoginScreen;
import ui.screens.RegisterScreen;
import ui.admin.AdminDashboard;
import ui.user.UserDashboard;
import model.Account;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    public static final String CARD_USER = "USER_DASH";
    public static final String CARD_ADMIN = "ADMIN_DASH";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    private LoginScreen loginScreen;
    private RegisterScreen registerScreen;
    private UserDashboard userDashboard;
    private AdminDashboard adminDashboard;

    public MainFrame() {
        setTitle("Simulasi Sistem Tanggap Darurat Pemadam Kebakaran");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setPreferredSize(new Dimension(1280, 760));

        rootPanel.setBackground(UITheme.BG_DARK);

        loginScreen = new LoginScreen(this);
        registerScreen = new RegisterScreen(this);
        userDashboard = new UserDashboard(this);
        adminDashboard = new AdminDashboard(this);

        rootPanel.add(loginScreen, CARD_LOGIN);
        rootPanel.add(registerScreen, CARD_REGISTER);
        rootPanel.add(userDashboard, CARD_USER);
        rootPanel.add(adminDashboard, CARD_ADMIN);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
        show(CARD_LOGIN);
    }

    public void show(String card) {
        cardLayout.show(rootPanel, card);
    }

    public void onLoginSuccess(Account user) {
        if (user.isAdmin()) {
            adminDashboard.refresh();
            show(CARD_ADMIN);
        } else {
            userDashboard.refresh();
            show(CARD_USER);
        }
    }

    public void onLogout() {
        show(CARD_LOGIN);
        loginScreen.reset();
    }
}
