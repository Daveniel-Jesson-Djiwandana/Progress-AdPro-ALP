import ui.MainFrame;
import ui.UITheme;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        service.AuthService.loadUsersFromFile();

        UITheme.apply();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        }); 
    }
}