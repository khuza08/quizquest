// File: quizquest.view.HomePage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomePage extends JFrame {
    private JButton btnLogin, btnStartQuiz;
    private Point initialClick;

    public HomePage() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setUndecorated(true);
        // Increased frame size to accommodate larger buttons
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));

        // Main rounded panel with added padding
        JPanel mainPanel = new RoundedPanel(32);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2)); // atur padding main panel

        // macos titlebar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 60));
        titleBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        // macos buttons
        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dotsPanel.setOpaque(false);

        JButton redDot = createMacOSDot(new Color(0xFF5F57), "Close");
        JButton yellowDot = createMacOSDot(new Color(0xFFBD2E), "Minimize");
        JButton greenDot = createMacOSDot(new Color(0x28CA42), "Maximize");

        dotsPanel.add(redDot);
        dotsPanel.add(yellowDot);
        dotsPanel.add(greenDot);

        titleBar.add(dotsPanel, BorderLayout.WEST);

        // content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        // Reduced insets to give more space to components
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        // Enable horizontal stretching
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // title
        JLabel titleLabel = new JLabel("Quizquest");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(Color.BLACK);

        // Center horizontally
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;         
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(titleLabel, gbc);

        // Spacer to push buttons down
        gbc.gridy = 1;
        gbc.weighty = 0.3;
        contentPanel.add(Box.createVerticalGlue(), gbc);

        // Login Button
        btnLogin = createStyledButton("Login");
        gbc.gridy = 2;
        gbc.weighty = 0;
        // Enable horizontal weight for this component
        gbc.weightx = 1.0;
        contentPanel.add(btnLogin, gbc);

        // Start Quiz Button
        btnStartQuiz = createStyledButton("Mulai Quiz (Tanpa Login)");
        gbc.gridy = 3;
        gbc.weighty = 0;
        // Enable horizontal weight for this component
        gbc.weightx = 1.0;
        contentPanel.add(btnStartQuiz, gbc);

        // === ASSEMBLY ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Event niggers
        btnLogin.addActionListener(e -> openLoginPage());
        btnStartQuiz.addActionListener(e -> openClassSelectionPage(null));
;

        makeDraggable(titleBar);
        makeDraggable(contentPanel);
    }
        private void openLoginPage() {
        new LoginPage().setVisible(true);
        dispose();
    }

    private void openClassSelectionPage(String username) {
        ClassSelectionPage frame = new ClassSelectionPage(username);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        dispose();
    }

    // Create macOS-style dot as a button
    private JButton createMacOSDot(Color color, String action) {
        JButton dot = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle background
                g2d.setColor(color);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                // Hover effect
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

        // Add hover animation
        dot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                dot.setPreferredSize(new Dimension(16, 16));
                dot.revalidate();
                dot.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dot.setPreferredSize(new Dimension(16, 16));
                dot.revalidate();
                dot.repaint();
            }
        });

        // Actions
        if ("Close".equals(action)) {
            dot.addActionListener(e -> System.exit(0));
        } else if ("Minimize".equals(action)) {
            dot.addActionListener(e -> setState(JFrame.ICONIFIED));
        } else if ("Maximize".equals(action)) {
            dot.addActionListener(e -> {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    setExtendedState(JFrame.NORMAL);
                } else {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }

        return dot;
    }

    // Custom panel with rounded corners
    private class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Create styled button with correct size
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x2D2D2D));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Set button dimensions
        btn.setPreferredSize(new Dimension(500, 90));
        btn.setMinimumSize(new Dimension(500, 90));
        
        // Hover effect
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

    // Make component draggable
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
                    int x = e.getXOnScreen() - initialClick.x;
                    int y = e.getYOnScreen() - initialClick.y;
                    setLocation(x, y);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomePage app = new HomePage();
            app.setVisible(true);
            // Ensure proper sizing of the frame
            app.pack();
        });
    }
}