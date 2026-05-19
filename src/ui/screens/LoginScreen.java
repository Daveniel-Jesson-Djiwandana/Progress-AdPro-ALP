package ui.screens;

import service.AuthService;
import model.Account;
import ui.MainFrame;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JPanel {

    private final MainFrame mainFrame;
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JLabel errorMessageLabel;

    public LoginScreen(MainFrame frame) {
        this.mainFrame = frame;
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        initializeUserInterface();
    }

    private void initializeUserInterface() {
        RoundedPanel loginCard = new RoundedPanel(UITheme.BG_CARD, 20);
        loginCard.setHasBorder(true);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        loginCard.setPreferredSize(new Dimension(420, 520));

        JLabel appIcon = new JLabel();
        appIcon.setIcon(new VectorIcon(VectorIcon.Type.FIRE, 52, UITheme.ACCENT_ORANGE));
        appIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel mainTitle = new JLabel("SIAGA KEBAKARAN", SwingConstants.CENTER);
        mainTitle.setFont(UITheme.FONT_TITLE);
        mainTitle.setForeground(UITheme.ACCENT_ORANGE);
        mainTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Simulasi Sistem Tanggap Darurat", SwingConstants.CENTER);
        subTitle.setFont(UITheme.FONT_BODY);
        subTitle.setForeground(UITheme.TEXT_SECONDARY);
        subTitle.setAlignmentX(CENTER_ALIGNMENT);

        usernameInput = new JTextField();
        applyInputFieldStyle(usernameInput);

        passwordInput = new JPasswordField();
        applyInputFieldStyle(passwordInput);

        errorMessageLabel = new JLabel(" ");
        errorMessageLabel.setFont(UITheme.FONT_SMALL);
        errorMessageLabel.setForeground(UITheme.DANGER);
        errorMessageLabel.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton loginButton = new RoundedButton("Masuk", UITheme.ACCENT_RED);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLoginAttempt());

        JButton registerLink = createNavigationLink("Belum punya akun? Daftar di sini");
        registerLink.addActionListener(e -> mainFrame.show(MainFrame.CARD_REGISTER));

        passwordInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleLoginAttempt();
            }
        });

        JLabel demoHint = new JLabel("<html><center>Demo: admin/admin123 · user/user123</center></html>",
                SwingConstants.CENTER);
        demoHint.setFont(UITheme.FONT_SMALL);
        demoHint.setForeground(UITheme.TEXT_MUTED);
        demoHint.setAlignmentX(CENTER_ALIGNMENT);

        loginCard.add(appIcon);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(mainTitle);
        loginCard.add(Box.createVerticalStrut(4));
        loginCard.add(subTitle);
        loginCard.add(Box.createVerticalStrut(28));

        addInputGroup(loginCard, "Username", usernameInput);
        addInputGroup(loginCard, "Password", passwordInput);

        loginCard.add(errorMessageLabel);
        loginCard.add(Box.createVerticalStrut(14));
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(12));
        loginCard.add(registerLink);
        loginCard.add(Box.createVerticalStrut(16));
        loginCard.add(demoHint);

        add(loginCard, new GridBagConstraints());
    }

    //LOGIC
    private void handleLoginAttempt() {
        String username = usernameInput.getText().trim();
        String password = new String(passwordInput.getPassword());

        Account account = AuthService.handleLogin(username, password);

        if (account != null) {
            errorMessageLabel.setText(" ");
            mainFrame.onLoginSuccess(account);
        } else {
            errorMessageLabel.setText("Username atau password salah.");
            passwordInput.setText("");
        }
    }

    public void reset() {
        usernameInput.setText("");
        passwordInput.setText("");
        errorMessageLabel.setText(" ");
    }

    //HUMANE HELPERS

    private void addInputGroup(JPanel container, String labelText, JComponent inputField) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_SUB);
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setAlignmentX(CENTER_ALIGNMENT);

        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(inputField);
        container.add(Box.createVerticalStrut(14));
    }

    private void applyInputFieldStyle(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBackground(UITheme.BG_SURFACE);
        field.setForeground(UITheme.TEXT_PRIMARY);
        field.setCaretColor(UITheme.ACCENT_ORANGE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(CENTER_ALIGNMENT);
    }

    private JButton createNavigationLink(String text) {
        JButton link = new JButton(text);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setForeground(UITheme.ACCENT_ORANGE);
        link.setFont(UITheme.FONT_SMALL);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(CENTER_ALIGNMENT);
        return link;
    }
}
