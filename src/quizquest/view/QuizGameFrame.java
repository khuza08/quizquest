// File: quizquest/view/QuizGameFrame.java
package quizquest.view;

import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.*;

public class QuizGameFrame extends JFrame {
    private static final Set<String> completedCategories = new HashSet<>(); // ← simpan kategori yang sudah dimainkan
    private static int lastClass = -1;
    private static int lastLevel = -1;

    private int className, level;
    private String username;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String selectedCategory;

    private String[] questions;
    private String[][] options;
    private int[] correctAnswers;
    private byte[][] imageDatas;

    private JPanel contentPanel; // Store content panel as instance variable
    private Point initialClick;

    public QuizGameFrame(int className, int level, String username) {
        this.className = className; // ← ADD THIS LINE
        this.level = level;         // ← ADD THIS LINE
        this.username = username;

        // ✅ Initialize FlatLaf (like in HomePage)
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Reset daftar jika kelas/level berbeda
        if (lastClass != className || lastLevel != level) {
            completedCategories.clear();
            lastClass = className;
            lastLevel = level;
        }

        setUndecorated(true); // ✅ Remove native title bar
        setSize(650, 500);
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
        titleBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 0, 15));

        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dotsPanel.setOpaque(false);

        JButton redDot = createMacOSDot(new Color(0xFF5F57), "Close");
        JButton yellowDot = createMacOSDot(new Color(0xFFBD2E), "Minimize");
        JButton greenDot = createMacOSDot(new Color(0x28CA42), "Maximize");

        dotsPanel.add(redDot);
        dotsPanel.add(yellowDot);
        dotsPanel.add(greenDot);
        titleBar.add(dotsPanel, BorderLayout.WEST);

        // === Content Panel ===
        contentPanel = new JPanel(new BorderLayout());

        selectedCategory = selectRandomCategory();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada soal tersisa untuk kelas " + className + " level " + level, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            navigateBack();
            return;
        }

        // Update title bar with category info
        JLabel titleLabel = new JLabel("Kuis [" + selectedCategory + "] - Kelas " + className + " Level " + level);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        if (loadQuestionsFromDatabase()) {
            displayQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "Tidak ada soal untuk kategori '" + selectedCategory + "'", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            navigateBack();
        }

        // === Assembly ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Draggable
        makeDraggable(titleBar);
        makeDraggable(contentPanel);
    }

    private String selectRandomCategory() {
        String sql = """
            SELECT DISTINCT category 
            FROM questions 
            WHERE class_level = ? AND quiz_level = ?
            """;

        java.util.List<String> availableCategories = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);) {

            stmt.setInt(1, className);
            stmt.setInt(2, level);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String cat = rs.getString("category");
                if (!completedCategories.contains(cat)) {
                    availableCategories.add(cat);
                }
            }

            if (availableCategories.isEmpty()) {
                // Jika semua kategori sudah dimainkan, reset
                completedCategories.clear();
                // Ambil ulang semua kategori
                rs.beforeFirst();
                while (rs.next()) {
                    availableCategories.add(rs.getString("category"));
                }
                if (availableCategories.isEmpty()) return null;
            }

            // Pilih acak
            Collections.shuffle(availableCategories, new Random());
            return availableCategories.get(0);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat kategori: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private boolean loadQuestionsFromDatabase() {
        String sql = """
            SELECT question_text, option_a, option_b, option_c, option_d, 
                   correct_option, image_data
            FROM questions 
            WHERE class_level = ? AND quiz_level = ? AND category = ?
            ORDER BY id
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

            stmt.setInt(1, className);
            stmt.setInt(2, level);
            stmt.setString(3, selectedCategory);

            ResultSet rs = stmt.executeQuery();

            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();

            if (count == 0) return false;

            questions = new String[count];
            options = new String[count][4];
            correctAnswers = new int[count];
            imageDatas = new byte[count][];

            int i = 0;
            while (rs.next()) {
                questions[i] = rs.getString("question_text");
                options[i][0] = rs.getString("option_a");
                options[i][1] = rs.getString("option_b");
                options[i][2] = rs.getString("option_c");
                options[i][3] = rs.getString("option_d");

                String correct = rs.getString("correct_option").toUpperCase();
                correctAnswers[i] = switch (correct) {
                    case "A" -> 0;
                    case "B" -> 1;
                    case "C" -> 2;
                    case "D" -> 3;
                    default -> 0;
                };

                imageDatas[i] = rs.getBytes("image_data");
                i++;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat soal: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.length) {
            // Tandai kategori sebagai selesai
            completedCategories.add(selectedCategory);
            showResult();
            return;
        }

        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JLabel categoryLabel = new JLabel("Kategori: " + selectedCategory);
        categoryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPanel.add(categoryLabel, BorderLayout.NORTH);

        if (imageDatas[currentQuestionIndex] != null && imageDatas[currentQuestionIndex].length > 0) {
            try {
                ImageIcon originalIcon = new ImageIcon(imageDatas[currentQuestionIndex]);
                Image scaledImage = originalIcon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contentPanel.add(imageLabel, BorderLayout.CENTER);
            } catch (Exception e) {
                System.err.println("Gagal muat gambar dari BLOB: " + e.getMessage());
            }
        }

        JLabel questionLabel = new JLabel((currentQuestionIndex + 1) + ". " + questions[currentQuestionIndex]);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(questionLabel, BorderLayout.SOUTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        String[] labels = {"A. ", "B. ", "C. ", "D. "};
        for (int i = 0; i < 4; i++) {
            JButton optionBtn = new JButton(labels[i] + options[currentQuestionIndex][i]);
            optionBtn.setHorizontalAlignment(SwingConstants.LEFT);
            optionBtn.setFocusPainted(false);
            final int optionIndex = i;
            optionBtn.addActionListener(e -> checkAnswer(optionIndex));
            optionsPanel.add(optionBtn);
        }

        contentPanel.add(optionsPanel, BorderLayout.EAST);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void checkAnswer(int selectedOption) {
        if (selectedOption == correctAnswers[currentQuestionIndex]) {
            score++;
            JOptionPane.showMessageDialog(this, "✅ Benar!", "Jawaban", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int correctIndex = correctAnswers[currentQuestionIndex];
            String correctText = options[currentQuestionIndex][correctIndex];
            JOptionPane.showMessageDialog(this,
                "❌ Nguwawor lho ya!\nJawaban yang benar: " + ("ABCD".charAt(correctIndex)) + ". " + correctText,
                "Jawaban", JOptionPane.WARNING_MESSAGE);
        }

        currentQuestionIndex++;
        displayQuestion(); // Now calls displayQuestion() without parameter
    }

    private void showResult() {
        int total = questions.length;
        int salah = total - score;
        int nilai = 100 - (salah * 5);
        String message = String.format(
            "Kategori: %s\nSkor Akhir:\nBenar: %d\nSalah: %d\nNilai: %d",
            selectedCategory, score, salah, nilai
        );

        if (username != null && !username.isEmpty()) {
            saveScoreToDatabase(score, total);
            message += "\n\n✅ Nilai telah disimpan!";
        }

        JOptionPane.showMessageDialog(this, message, "Hasil Kuis", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        navigateBack();
    }

    private void saveScoreToDatabase(int score, int total) {
        String sql = """
            INSERT INTO scores (user_id, username, class_level, quiz_level, score, total_questions, category)
            SELECT id, ?, ?, ?, ?, ?, ?
            FROM users WHERE username = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, className);
            stmt.setInt(3, level);
            stmt.setInt(4, score);
            stmt.setInt(5, total);
            stmt.setString(6, selectedCategory);
            stmt.setString(7, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void navigateBack() {
        if (username != null && !username.isEmpty()) {
            new UserHomePage().setVisible(true);
        } else {
            new HomePage().setVisible(true);
        }
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

        dot.setPreferredSize(new Dimension(12, 12));
        dot.setMaximumSize(new Dimension(12, 12));
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