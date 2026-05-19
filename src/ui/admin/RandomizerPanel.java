package ui.admin;

import database.Database;
import model.Incident;
import service.IncidentService;
import ui.UITheme;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.VectorIcon;

import javax.swing.*;
import java.awt.*;

public class RandomizerPanel extends JPanel {

    private JLabel lblResult;
    private JPanel resultCard;

    public RandomizerPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        // Title
        JLabel title = new JLabel("  Acak Insiden Kebakaran");
        title.setIcon(new VectorIcon(VectorIcon.Type.DICE, 24, UITheme.TEXT_PRIMARY));
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Buat insiden simulasi secara acak untuk melatih respons petugas.");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(sub);

        // Centre area
        RoundedPanel centre = new RoundedPanel(UITheme.BG_CARD, 20);
        centre.setHasBorder(true);
        centre.setLayout(new GridBagLayout());
        centre.setPreferredSize(new Dimension(500, 300));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel fireIcon = new JLabel();
        fireIcon.setIcon(new VectorIcon(VectorIcon.Type.FIRE, 54, UITheme.ACCENT_ORANGE));
        fireIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel prompt = new JLabel("Klik tombol di bawah untuk membuat insiden simulasi baru");
        prompt.setFont(UITheme.FONT_BODY);
        prompt.setForeground(UITheme.TEXT_SECONDARY);
        prompt.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton btnRandom = new RoundedButton("  Acak Insiden Sekarang", UITheme.ACCENT_RED);
        btnRandom.setIcon(new VectorIcon(VectorIcon.Type.DICE, 18, UITheme.TEXT_PRIMARY));
        btnRandom.setPreferredSize(new Dimension(240, 44));
        btnRandom.setMaximumSize(new Dimension(280, 44));
        btnRandom.setAlignmentX(CENTER_ALIGNMENT);
        btnRandom.addActionListener(e -> randomize());

        inner.add(fireIcon);
        inner.add(Box.createVerticalStrut(12));
        inner.add(prompt);
        inner.add(Box.createVerticalStrut(20));
        inner.add(btnRandom);

        centre.add(inner);

        // Result card (hidden until first randomize)
        resultCard = buildResultCard();
        resultCard.setVisible(false);

        JPanel mainArea = new JPanel(new BorderLayout(0, 16));
        mainArea.setOpaque(false);
        mainArea.add(centre, BorderLayout.NORTH);
        mainArea.add(resultCard, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);
        add(mainArea, BorderLayout.CENTER);
    }

    private JPanel buildResultCard() {
        RoundedPanel card = new RoundedPanel(new Color(60, 20, 20), 16);
        card.setHasBorder(true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel header = new JLabel("  Insiden Baru Dibuat");
        header.setIcon(new VectorIcon(VectorIcon.Type.ALERT, 18, UITheme.ACCENT_ORANGE));
        header.setFont(UITheme.FONT_SUB);
        header.setForeground(UITheme.ACCENT_ORANGE);

        lblResult = new JLabel("<html></html>");
        lblResult.setFont(UITheme.FONT_BODY);
        lblResult.setForeground(UITheme.TEXT_PRIMARY);

        card.add(header, BorderLayout.NORTH);
        card.add(lblResult, BorderLayout.CENTER);
        return card;
    }

    private void randomize() {
        String adminName = Database.getCurrentUser() != null ? Database.getCurrentUser().getName() : "Admin";
        Incident inc = IncidentService.randomizeIncident(adminName);

        String html = String.format(
            "<html>" +
            "<b>ID:</b> %s<br>" +
            "<b>Lokasi:</b> %s<br>" +
            "<b>Tingkat:</b> %s &nbsp; <b>Intensitas:</b> %d/10<br>" +
            "<b>Korban Terjebak:</b> %d orang<br>" +
            "<b>Luas Api:</b> %.0f m²<br>" +
            "<b>Deskripsi:</b> %s<br>" +
            "<b>Skor Prioritas:</b> %.1f" +
            "</html>",
            inc.getIncidentId(), inc.getLocation(),
            inc.getSeverity().getLabel(), inc.getFireIntensity(),
            inc.getNumVictimsTrapped(), inc.getFireSpreadArea(),
            inc.getDescription(), inc.getPriorityScore()
        );
        lblResult.setText(html);
        resultCard.setVisible(true);
        revalidate(); repaint();
    }
}
