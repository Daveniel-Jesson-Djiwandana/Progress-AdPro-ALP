package ui.user;

import javax.swing.*;
import java.awt.*;

public class UserDashboard extends JPanel {

    public UserDashboard(Object mainFrame) {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#0f3460"));

        JLabel label = new JLabel("USER DASHBOARD", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 32));

        JLabel sub = new JLabel("(UI coming soon)", SwingConstants.CENTER);
        sub.setForeground(Color.LIGHT_GRAY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setBackground(Color.decode("#0f3460"));
        center.add(label);
        center.add(sub);

        add(center, BorderLayout.CENTER);
    }

   
    public void refresh() {};
}