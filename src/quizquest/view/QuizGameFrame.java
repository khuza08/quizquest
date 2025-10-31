// File: quizquest/view/QuizGameFrame.java
package quizquest.view;

import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

    public QuizGameFrame(int className, int level, String username) {
        this.className = className;
        this.level = level;
        this.username = username;

        // Reset daftar jika kelas/level berbeda
        if (lastClass != className || lastLevel != level) {
            completedCategories.clear();
            lastClass = className;
            lastLevel = level;
        }

        setTitle("Kuis Kelas " + className + " - Level " + level);
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedCategory = selectRandomCategory();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada soal tersisa untuk kelas " + className + " level " + level, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            navigateBack();
            return;
        }

        setTitle("Kuis [" + selectedCategory + "] - Kelas " + className + " Level " + level);

        if (loadQuestionsFromDatabase()) {
            displayQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "Tidak ada soal untuk kategori '" + selectedCategory + "'", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            navigateBack();
        }
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

        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel categoryLabel = new JLabel("Kategori: " + selectedCategory);
        categoryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(categoryLabel, BorderLayout.NORTH);

        if (imageDatas[currentQuestionIndex] != null && imageDatas[currentQuestionIndex].length > 0) {
            try {
                ImageIcon originalIcon = new ImageIcon(imageDatas[currentQuestionIndex]);
                Image scaledImage = originalIcon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                add(imageLabel, BorderLayout.CENTER);
            } catch (Exception e) {
                System.err.println("Gagal muat gambar dari BLOB: " + e.getMessage());
            }
        }

        JLabel questionLabel = new JLabel((currentQuestionIndex + 1) + ". " + questions[currentQuestionIndex]);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(questionLabel, BorderLayout.SOUTH);

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

        add(optionsPanel, BorderLayout.EAST);
        revalidate();
        repaint();
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
        displayQuestion();
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
}