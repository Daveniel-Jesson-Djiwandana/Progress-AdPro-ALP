package ui.admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JPanel {

    public AdminDashboard(Object mainFrame) {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#1a1a2e"));

        JLabel label = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 32));

        JLabel sub = new JLabel("(UI coming soon)", SwingConstants.CENTER);
        sub.setForeground(Color.LIGHT_GRAY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setBackground(Color.decode("#1a1a2e"));
        center.add(label);
        center.add(sub);

        add(center, BorderLayout.CENTER);
    }
    public void refresh() {
    
    }
}