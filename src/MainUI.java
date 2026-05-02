import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class MainUI extends JFrame {
    private JTextField txtActivity;
    private JList<String> activityList;
    private DefaultListModel<String> listModel;
    private JLabel lblCredits;

    public MainUI() {
        setTitle("Tymzy 🎯 Activity Logger");
        setSize(650, 500);  // ✅ bigger window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(30, 30, 60)); // dark gaming style


        // ---------- COLORS & FONT ----------
        Font headerFont = new Font("Arial", Font.BOLD, 22);
        Font normalFont = new Font("Verdana", Font.PLAIN, 14);

        JLabel lblWelcome = new JLabel("Welcome to Tymzy !");
        lblWelcome.setBounds(20, 10, 400, 30);
        lblWelcome.setFont(headerFont);
        lblWelcome.setForeground(new Color(0, 102, 204));
        add(lblWelcome);

        txtActivity = new JTextField();
        txtActivity.setBounds(20, 50, 250, 32);
        txtActivity.setFont(normalFont);
        add(txtActivity);

        // ---------- LOAD + SCALE ICONS ----------
        ImageIcon addIcon    = resizeIcon("/icons/tick.png", 22, 22);
        ImageIcon deleteIcon = resizeIcon("/icons/cross.png", 22, 22);
        ImageIcon useIcon    = resizeIcon("/icons/controller.png", 22, 22);
        ImageIcon homeIcon   = resizeIcon("/icons/home.png", 22, 22);

        JButton btnAdd = new JButton(" Add Activity", addIcon);
        btnAdd.setBounds(290, 50, 150, 32);
        styleButton(btnAdd, new Color(76, 175, 80));
        add(btnAdd);

        JButton btnDelete = new JButton(" Delete Activity", deleteIcon);
        btnDelete.setBounds(450, 50, 160, 32);
        styleButton(btnDelete, new Color(244, 67, 54));
        add(btnDelete);

        listModel = new DefaultListModel<>();
        activityList = new JList<>(listModel);
        activityList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // custom renderer for alternating row color
        activityList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(new Color(100, 149, 237)); // blue selection
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground((index % 2 == 0) ? new Color(230, 240, 250) : new Color(255, 245, 238));
                label.setForeground(new Color(50, 50, 50));
            }
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            return label;
        });

        JScrollPane scroll = new JScrollPane(activityList);
        scroll.setBounds(20, 100, 590, 250); // ✅ more space
        add(scroll);

        lblCredits = new JLabel("Credits: 0 mins");
        lblCredits.setBounds(20, 370, 250, 32);
        lblCredits.setFont(new Font("Verdana", Font.BOLD, 15));
        lblCredits.setForeground(new Color(128, 0, 128));
        add(lblCredits);

        JButton btnUse = new JButton(" Use Credits", useIcon);
        btnUse.setBounds(290, 370, 160, 32);
        styleButton(btnUse, new Color(33, 150, 243));
        add(btnUse);

        JButton btnHome = new JButton(" Home", homeIcon);
        btnHome.setBounds(460, 370, 150, 32);
        styleButton(btnHome, new Color(255, 140, 0)); // orange
        add(btnHome);

        // ---------- INITIAL LOAD ----------
        loadPastActivities();
        updateCreditsLabel();

        // ---------- BUTTON ACTIONS ----------
        btnAdd.addActionListener(e -> {
            String activity = txtActivity.getText().trim();
            if (!activity.isEmpty()) {
                new SwingWorker<Void, Void>() {
                    int earnedCredits = calculateCreditsFromTask(activity);

                    @Override
                    protected Void doInBackground() {
                        DBHelper.insertActivity(activity);
                        DBHelper.addCredits(earnedCredits);
                        return null;
                    }

                    @Override
                    protected void done() {
                        loadPastActivities();
                        updateCreditsLabel();
                        JOptionPane.showMessageDialog(MainUI.this,
                                "Task added ✅ +" + earnedCredits + " mins credits earned!");
                        txtActivity.setText("");
                    }
                }.execute();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter an activity!");
            }
        });

        btnDelete.addActionListener(e -> {
            String selected = activityList.getSelectedValue();
            if (selected != null) {
                String activityName = selected.split("\\(")[0].trim();
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        DBHelper.deleteActivity(activityName);
                        return null;
                    }

                    @Override
                    protected void done() {
                        loadPastActivities();
                    }
                }.execute();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an activity to delete.");
            }
        });

        btnUse.addActionListener(e -> {
            String minsStr = JOptionPane.showInputDialog(this, "Enter minutes to use:");
            if (minsStr == null) return;
            int mins;
            try {
                mins = Integer.parseInt(minsStr.trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Enter a valid number!");
                return;
            }
            if (mins <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a positive number!");
                return;
            }

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return DBHelper.useCredits(mins);
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            updateCreditsLabel();
                            JOptionPane.showMessageDialog(MainUI.this, "Enjoy your leisure! 🎉 -" + mins + " mins");
                        } else {
                            JOptionPane.showMessageDialog(MainUI.this, "Not enough credits!");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainUI.this, "Error using credits: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        btnHome.addActionListener(e -> {
            dispose();
            HomePage home = new HomePage();
            home.setLocationRelativeTo(null);
            home.setVisible(true);
        });
    }

    // ✅ Utility to scale icons
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
        btn.setBorder(new LineBorder(new Color(200, 200, 200)));
        btn.setFont(new Font("Tahoma", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT); // icon + text aligned
    }

    private void loadPastActivities() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return DBHelper.getAllActivities();
            }

            @Override
            protected void done() {
                try {
                    List<String> pastActivities = get();
                    listModel.clear();
                    for (String act : pastActivities) {
                        listModel.addElement(act);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainUI.this, "Error loading activities: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void updateCreditsLabel() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return DBHelper.getCredits();
            }

            @Override
            protected void done() {
                try {
                    int credits = get();
                    lblCredits.setText("Credits: " + credits + " mins");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainUI.this, "Error updating credits: " + e.getMessage());
                }
            }
        }.execute();
    }

    private int calculateCreditsFromTask(String activity) {
        activity = activity.toLowerCase();
        if (activity.contains("hour")) {
            return 30;
        } else if (activity.contains("30 min") || activity.contains("30min")) {
            return 15;
        } else {
            return 10; // default
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI ui = new MainUI();
            ui.setLocationRelativeTo(null);
            ui.setVisible(true);
        });
    }
}
