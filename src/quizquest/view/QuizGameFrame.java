// File: quizquest.view.QuizGameFrame.java
package quizquest.view;

import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class QuizGameFrame extends JFrame {
    private int className, level;
    private String username; // null jika tidak login
    private int currentQuestionIndex = 0;
    private int score = 0;

    // Data soal dari database
    private String[] questions;
    private String[][] options;
    private int[] correctAnswers; // 0=A, 1=B, 2=C, 3=D

    public QuizGameFrame(int className, int level, String username) {
        this.className = className;
        this.level = level;
        this.username = username;

        setTitle("Kuis Kelas " + className + " - Level " + level);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        if (loadQuestionsFromDatabase()) {
            displayQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "Tidak ada soal untuk kelas " + className + " level " + level, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new HomePage().setVisible(true);
        }
    }

private boolean loadQuestionsFromDatabase() {
    String sql = "SELECT question_text, option_a, option_b, option_c, option_d, correct_option " +
                 "FROM questions " +
                 "WHERE class_level = ? AND quiz_level = ? " +
                 "ORDER BY id";

    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(sql,
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY);

        stmt.setInt(1, className);
        stmt.setInt(2, level);

        ResultSet rs = stmt.executeQuery();

        // Hitung jumlah soal
        rs.last();
        int count = rs.getRow();
        rs.beforeFirst();

        if (count == 0) return false;

        // Inisialisasi array
        questions = new String[count];
        options = new String[count][4];
        correctAnswers = new int[count];

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
            showResult();
            return;
        }

        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JLabel questionLabel = new JLabel((currentQuestionIndex + 1) + ". " + questions[currentQuestionIndex]);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(questionLabel, BorderLayout.NORTH);

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

        add(optionsPanel, BorderLayout.CENTER);
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
        double percentage = (double) score / total * 100;
        String message = String.format("Skor Akhir:\n%d / %d (%.1f%%)", score, total, percentage);

        // Simpan ke database jika user login
        if (username != null && !username.isEmpty()) {
            saveScoreToDatabase(score, total);
            message += "\n\n✅ Nilai telah disimpan!";
        }

        JOptionPane.showMessageDialog(this, message, "Hasil Kuis", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new HomePage().setVisible(true);
    }

    private void saveScoreToDatabase(int score, int total) {
        String sql = """
            INSERT INTO scores (user_id, username, class_level, quiz_level, score, total_questions)
            SELECT id, ?, ?, ?, ?, ?
            FROM users WHERE username = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, className);
            stmt.setInt(3, level);
            stmt.setInt(4, score);
            stmt.setInt(5, total);
            stmt.setString(6, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Jangan tampilkan error ke user — cukup log
        }
    }
}