package ui.screens;

import service.AuthService;
import ui.MainFrame;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.VectorIcon;
import javax.swing.*;
import java.awt.*;

public class RegisterScreen extends JPanel {
    private final MainFrame mainFrame;

    private JTextField nameInput, emailInput, usernameInput, phoneInput, badgeNumberInput, rankInput;
    private JPasswordField passwordInput;
    private JComboBox<String> roleSelector;
    private JLabel errorMessageLabel;
    private JPanel adminSpecificFields;

    public RegisterScreen(MainFrame frame) {
        this.mainFrame = frame;
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        initializeUserInterface();
    }

    private void initializeUserInterface() {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.BG_CARD);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        JLabel screenTitle = new JLabel("Daftar Akun Baru");
        screenTitle.setFont(UITheme.FONT_HEADING);
        screenTitle.setForeground(UITheme.ACCENT_ORANGE);
        screenTitle.setAlignmentX(CENTER_ALIGNMENT);

        cardPanel.add(screenTitle);
        cardPanel.add(Box.createVerticalStrut(25));

        nameInput = createFormInputField(cardPanel, "Nama Lengkap");
        emailInput = createFormInputField(cardPanel, "Email");
        usernameInput = createFormInputField(cardPanel, "Username");

        passwordInput = new JPasswordField();
        applyInputFieldStyle(passwordInput);
        addLabelAndComponent(cardPanel, "Password", passwordInput);

        phoneInput = createFormInputField(cardPanel, "No. Telepon");

        roleSelector = new JComboBox<>(new String[] { "Pengguna (User)", "Petugas Pemadam (Admin)" });
        applyComboBoxStyle(roleSelector);
        addLabelAndComponent(cardPanel, "Daftar sebagai", roleSelector);

        adminSpecificFields = new JPanel();
        adminSpecificFields.setOpaque(false);
        adminSpecificFields.setLayout(new BoxLayout(adminSpecificFields, BoxLayout.Y_AXIS));

        badgeNumberInput = createFormInputField(adminSpecificFields, "Nomor Lencana");
        rankInput = createFormInputField(adminSpecificFields, "Jabatan/Pangkat");

        adminSpecificFields.setVisible(false);

        roleSelector.addActionListener(event -> {
            boolean isAdminSelected = roleSelector.getSelectedIndex() == 1;
            adminSpecificFields.setVisible(isAdminSelected);
            revalidate();
        });

        cardPanel.add(adminSpecificFields);

        errorMessageLabel = new JLabel(" ");
        errorMessageLabel.setFont(UITheme.FONT_SMALL);
        errorMessageLabel.setForeground(UITheme.DANGER);
        errorMessageLabel.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton registerButton = new RoundedButton("Daftar", UITheme.ACCENT_RED);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        registerButton.setAlignmentX(CENTER_ALIGNMENT);
        registerButton.addActionListener(event -> handleRegistrationAttempt());

        JButton backToLoginLink = createNavigationLink(" Kembali ke Login");
        backToLoginLink.setIcon(new VectorIcon(VectorIcon.Type.BACK, 12, UITheme.ACCENT_ORANGE));
        backToLoginLink.addActionListener(event -> mainFrame.show(MainFrame.CARD_LOGIN));

        cardPanel.add(errorMessageLabel);
        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(backToLoginLink);

        JScrollPane scrollContainer = new JScrollPane(cardPanel);
        scrollContainer.setPreferredSize(new Dimension(480, 620));
        scrollContainer.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scrollContainer.getViewport().setBackground(UITheme.BG_CARD);
        scrollContainer.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollContainer, new GridBagConstraints());
    }

    // HUMANE HELPERS

    private JTextField createFormInputField(JPanel container, String labelText) {
        JTextField newField = new JTextField();
        applyInputFieldStyle(newField);
        addLabelAndComponent(container, labelText, newField);
        return newField;
    }

    private void addLabelAndComponent(JPanel container, String text, JComponent component) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SUB);
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setAlignmentX(CENTER_ALIGNMENT);

        container.add(label);
        container.add(Box.createVerticalStrut(5));
        container.add(component);
        container.add(Box.createVerticalStrut(12));
    }

    private void applyInputFieldStyle(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBackground(UITheme.BG_SURFACE);
        field.setForeground(UITheme.TEXT_PRIMARY);
        field.setCaretColor(UITheme.ACCENT_ORANGE);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(CENTER_ALIGNMENT);
    }

    private void applyComboBoxStyle(JComboBox<?> comboBox) {
        comboBox.setFont(UITheme.FONT_BODY);
        comboBox.setBackground(UITheme.BG_SURFACE);
        comboBox.setForeground(UITheme.TEXT_PRIMARY);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        comboBox.setAlignmentX(CENTER_ALIGNMENT);
    }

    private JButton createNavigationLink(String linkText) {
        JButton link = new JButton(linkText);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setForeground(UITheme.ACCENT_ORANGE);
        link.setFont(UITheme.FONT_SMALL);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(CENTER_ALIGNMENT);
        return link;
    }

    // LOGIC

    private void handleRegistrationAttempt() {
        String name = nameInput.getText().trim();
        String email = emailInput.getText().trim();
        String username = usernameInput.getText().trim();
        String password = new String(passwordInput.getPassword());
        String phone = phoneInput.getText().trim();
        boolean isAdmin = roleSelector.getSelectedIndex() == 1;

        String resultMessage;
        if (isAdmin) {
            resultMessage = AuthService.registerNewAdmin(name, email, username, password, phone,
                    badgeNumberInput.getText().trim(), rankInput.getText().trim());
        } else {
            resultMessage = AuthService.registerNewUser(name, email, username, password, phone);
        }

        if (resultMessage != null) {
            errorMessageLabel.setText(resultMessage);
        } else {
            JOptionPane.showMessageDialog(this, "Akun berhasil dibuat!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.show(MainFrame.CARD_LOGIN);
            resetAllFields();
        }
    }

    private void resetAllFields() {
        JTextField[] allFields = { nameInput, emailInput, usernameInput, passwordInput, phoneInput, badgeNumberInput,
                rankInput };
        for (JTextField field : allFields) {
            field.setText("");
        }
        errorMessageLabel.setText(" ");
        roleSelector.setSelectedIndex(0);
    }
}
