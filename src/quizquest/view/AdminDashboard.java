// quizquest.view.AdminDashboard.java
package quizquest.view;

import quizquest.frames.ManageQuestionsFrame;
import quizquest.frames.ManageUsersFrame;
import quizquest.frames.ViewScoresFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {
    private JButton btnManageUsers, btnManageQuestions, btnViewScores, btnLogout;
    private Point initialClick;

    public AdminDashboard() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setUndecorated(true);
        setSize(480, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        titleBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

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
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        // Spacer
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Create buttons and add them responsively
        btnManageUsers = createStyledButton("Kelola Siswa", 60);
        addResponsiveButton(contentPanel, btnManageUsers);

        btnManageQuestions = createStyledButton("Kelola Soal", 60);
        addResponsiveButton(contentPanel, btnManageQuestions);

        btnViewScores = createStyledButton("Lihat Nilai Siswa", 60);
        addResponsiveButton(contentPanel, btnViewScores);

        btnLogout = createStyledButton("Logout", 60);
        addResponsiveButton(contentPanel, btnLogout);

        // === Assembly ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

// Event handlers - now the fields exist
btnManageUsers.addActionListener(e -> {
    ManageUsersFrame frame = new ManageUsersFrame();
    frame.setLocationRelativeTo(null); // ← Add this line
    frame.setVisible(true);
});

btnManageQuestions.addActionListener(e -> {
    ManageQuestionsFrame frame = new ManageQuestionsFrame();
    frame.setLocationRelativeTo(null); // ← Add this line
    frame.setVisible(true);
});

btnViewScores.addActionListener(e -> {
    ViewScoresFrame frame = new ViewScoresFrame();
    frame.setLocationRelativeTo(null); // ← Add this line
    frame.setVisible(true);
});

btnLogout.addActionListener(e -> {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Apakah Anda yakin ingin logout?",
        "Konfirmasi Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    if (confirm == JOptionPane.YES_OPTION) {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            window.dispose();
        }
        new HomePage().setVisible(true);
    }
});

        // Draggable
        makeDraggable(titleBar);
        makeDraggable(contentPanel);
    }

    private void addResponsiveButton(JPanel parent, JButton button) {
        // Create a panel for this button
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12)); // 12px left/right padding

        buttonWrapper.add(button, BorderLayout.CENTER);
        parent.add(buttonWrapper);
    }

    // === Helper Methods ===

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

        dot.setPreferredSize(new Dimension(14, 14));
        dot.setMaximumSize(new Dimension(14, 14));
        dot.setContentAreaFilled(false);
        dot.setBorderPainted(false);
        dot.setFocusPainted(false);
        dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if ("Close".equals(action)) {
            dot.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    AdminDashboard.this,
                    "Apakah Anda yakin ingin keluar?",
                    "Konfirmasi Keluar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        window.dispose();
                    }
                    System.exit(0);
                }
            });
        } else if ("Minimize".equals(action)) {
            dot.addActionListener(e -> setState(JFrame.ICONIFIED));
        }

        return dot;
    }

    private JButton createStyledButton(String text, int height) {
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

        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, height)); // Let parent decide width
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); // Allow stretching

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
}