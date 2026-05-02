import javax.swing.*;
import java.awt.*;

public class HomePage extends JFrame {
    public HomePage() {
        setTitle("Tymzy 🎮 Home");
        setSize(700, 550);  // ✅ bigger window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        getContentPane().setBackground(new Color(30, 30, 60)); // dark gaming style

        // ---------- Title ----------
        JLabel title = new JLabel("Tymzy", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));  // ✅ bigger title
        title.setForeground(Color.WHITE);
        title.setBounds(150, 60, 400, 60);
        add(title);

        // ---------- Tagline ----------
        JLabel tagline = new JLabel("\"Because effort deserves entertainment.\"", SwingConstants.CENTER);
        tagline.setFont(new Font("Verdana", Font.ITALIC, 18));  // ✅ stylish tagline
        tagline.setForeground(new Color(173, 216, 230));        // light bluish text
        tagline.setBounds(100, 120, 500, 30);
        add(tagline);

        // load + scale icons (make sure PNGs are inside src/icons/)
        ImageIcon startIcon = resizeIcon("/icons/play.png", 40, 40);   // ✅ bigger icons
        ImageIcon statsIcon = resizeIcon("/icons/trophy.png", 40, 40);
        ImageIcon exitIcon  = resizeIcon("/icons/exit.png", 40, 40);

        JButton btnStart = new JButton("      Start", startIcon);
        btnStart.setBounds(220, 180, 250, 60);  // ✅ shifted down
        styleButton(btnStart, new Color(76, 175, 80));
        add(btnStart);

        JButton btnStats = new JButton("  View Stats", statsIcon);
        btnStats.setBounds(220, 260, 250, 60);
        styleButton(btnStats, new Color(33, 150, 243));
        add(btnStats);

        JButton btnExit = new JButton("      Exit", exitIcon);
        btnExit.setBounds(220, 340, 250, 60);
        styleButton(btnExit, new Color(244, 67, 54));
        add(btnExit);

        // button actions
        btnStart.addActionListener(e -> {
            dispose(); // close home
            MainUI mainUI = new MainUI();
            mainUI.setLocationRelativeTo(null);
            mainUI.setVisible(true);
        });

        btnStats.addActionListener(e -> {
            int credits = DBHelper.getCredits();
            JOptionPane.showMessageDialog(this,
                    "🏆 Your current credits: " + credits + " mins",
                    "Player Stats", JOptionPane.INFORMATION_MESSAGE);
        });

        btnExit.addActionListener(e -> System.exit(0));
    }

    // ✅ Utility method to scale icons
    private ImageIcon resizeIcon(String path, int width, int height) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("⚠ Icon not found: " + path);
            return null;
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 20)); // ✅ bigger font
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT); // text aligned beside icon
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomePage home = new HomePage();
            home.setLocationRelativeTo(null);
            home.setVisible(true);
        });
    }
}
