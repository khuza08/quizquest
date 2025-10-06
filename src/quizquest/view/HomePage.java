// File: quizquest.view/HomePage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomePage extends JFrame {
    private JButton btnLogin, btnStartQuiz;

    public HomePage() {
        setTitle("Quiz Quest - Bahasa Inggris SMP");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to Quiz Quest!", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        btnLogin = new JButton("Login");
        btnStartQuiz = new JButton("Mulai Kuis (Tanpa Login)");

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnStartQuiz);

        add(buttonPanel, BorderLayout.CENTER);

        // Event
        btnLogin.addActionListener(e -> openLoginPage());
        btnStartQuiz.addActionListener(e -> openClassSelectionPage(false));

        showLoadingScreen();
    }

    private void showLoadingScreen() {
        JOptionPane.showMessageDialog(this, "Loading...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openLoginPage() {
        new LoginPage().setVisible(true);
        dispose(); // Tutup home page
    }

    private void openClassSelectionPage(boolean isLoggedIn) {
        new ClassSelectionPage(isLoggedIn).setVisible(true);
        dispose();
    }
}