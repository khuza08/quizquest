// File: quizquest.view.LoginPage.java
package quizquest.view;

import quizquest.Main;
import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnBack;
    private Point initialClick;

    public LoginPage() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setUndecorated(true);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));

        // Main rounded panel
        JPanel mainPanel = new RoundedPanel(32);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));

        // === macOS Title Bar ===
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 40));
        titleBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dotsPanel.setOpaque(false);

        JButton redDot = createMacOSDot(new Color(0xFF5F57), "Close");
        JButton yellowDot = createMacOSDot(new Color(0xFFBD2E), "Minimize");
        JButton greenDot = createMacOSDot(new Color(0x28CA42), "Maximize");

        dotsPanel.add(redDot);
        dotsPanel.add(yellowDot);
        dotsPanel.add(greenDot);
        titleBar.add(dotsPanel, BorderLayout.WEST);

        // === Content Panel ===
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(titleLabel, gbc);

        // Username label + field
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(new JLabel("Username:"), gbc);

        gbc.gridy = 2;
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(txtUsername, gbc);

        // Password label + field
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 4;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(txtPassword, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        btnLogin = createStyledButton("Login", 150, 50);
        btnBack = createStyledButton("Kembali", 150, 50);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnBack);
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 0, 0);
        contentPanel.add(buttonPanel, gbc);

        // === Assembly ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Actions
        btnLogin.addActionListener(e -> validateLogin());
        btnBack.addActionListener(e -> {
            dispose();
            new HomePage().setVisible(true);
        });

        // Key bindings
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ENTER"), "login");
        getRootPane().getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateLogin();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getRootPane().getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new HomePage().setVisible(true);
            }
        });

        // Draggable
        makeDraggable(titleBar);
        makeDraggable(contentPanel);
    }

    // === Helper Methods (same as HomePage) ===

    private JButton createMacOSDot(Color color, String action) {
        JButton dot = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        dot.setPreferredSize(new Dimension(16, 16));
        dot.setMaximumSize(new Dimension(16, 16));
        dot.setContentAreaFilled(false);
        dot.setBorderPainted(false);
        dot.setFocusPainted(false);
        dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if ("Close".equals(action)) {
            dot.addActionListener(e -> dispose());
        } else if ("Minimize".equals(action)) {
            dot.addActionListener(e -> setState(JFrame.ICONIFIED));
        }

        return dot;
    }

    private JButton createStyledButton(String text, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x2D2D2D));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0x404040));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0x2D2D2D));
            }
        });

        return btn;
    }

    private void makeDraggable(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        comp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    setLocation(e.getXOnScreen() - initialClick.x, e.getYOnScreen() - initialClick.y);
                }
            }
        });
    }

    private class RoundedPanel extends JPanel {
        private int radius;
        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // === Login Logic (unchanged) ===

    private void validateLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT username, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbUsername = rs.getString("username");
                    String role = rs.getString("role");

                    Main.CURRENT_USER = dbUsername;

                    JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + role + "!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                    if ("admin".equals(role)) {
                        new AdminDashboard().setVisible(true);
                    } else if ("siswa".equals(role)) {
                        new UserHomePage().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Role tidak dikenali!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Username atau password salah!", "Gagal Login", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error koneksi database:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}