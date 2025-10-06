// File: quizquest.view/QuizGameFrame.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QuizGameFrame extends JFrame {
    private int className, level;
    private boolean isLoggedIn;
    private int currentQuestionIndex = 0;
    private int score = 0;

    // Dummy soal
    private String[] questions = {"What is the capital of Indonesia?", "What is 2+2?"};
    private String[][] options = {
        {"Jakarta", "Bandung", "Surabaya", "Medan"},
        {"3", "4", "5", "6"}
    };
    private int[] correctAnswers = {0, 1}; // index jawaban benar

    public QuizGameFrame(int className, int level, boolean isLoggedIn) {
        this.className = className;
        this.level = level;
        this.isLoggedIn = isLoggedIn;

        setTitle("Kuis Kelas " + className + " - Level " + level);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.length) {
            showResult();
            return;
        }

        setLayout(new BorderLayout());

        JLabel questionLabel = new JLabel(questions[currentQuestionIndex]);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
        for (int i = 0; i < 4; i++) {
            JButton optionBtn = new JButton(options[currentQuestionIndex][i]);
            final int optionIndex = i;
            optionBtn.addActionListener(e -> checkAnswer(optionIndex));
            optionsPanel.add(optionBtn);
        }

        add(optionsPanel, BorderLayout.CENTER);
    }

    private void checkAnswer(int selectedOption) {
        if (selectedOption == correctAnswers[currentQuestionIndex]) {
            score++;
            JOptionPane.showMessageDialog(this, "Benar!");
        } else {
            JOptionPane.showMessageDialog(this, "Salah!");
        }

        currentQuestionIndex++;
        displayQuestion();
    }

    private void showResult() {
        String message = "Skor Anda: " + score + "/" + questions.length;
        if (isLoggedIn) {
            message += "\n\nHistory nilai Anda telah disimpan.";
        }
        JOptionPane.showMessageDialog(this, message);
        dispose();

        new HomePage().setVisible(true);
    }
}