// File: quizquest.view.UserHomePage.java
package quizquest.view;

import quizquest.Main;

import javax.swing.*;
import java.awt.*;

public class UserHomePage extends JFrame {
    public UserHomePage() {
        setTitle("User Home - Quiz Quest");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Ambil username dari session (pasti tidak null karena hanya dibuka setelah login)
        String username = Main.CURRENT_USER != null ? Main.CURRENT_USER : "Siswa";
        JLabel header = new JLabel("Halo, " + username + "!", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(header, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnStartQuiz = new JButton("Mulai Kuis");
        JButton btnViewHistory = new JButton("Lihat History Nilai");
        JButton btnLogout = new JButton("Logout");

        buttonPanel.add(btnStartQuiz);
        buttonPanel.add(btnViewHistory);
        buttonPanel.add(btnLogout);

        add(buttonPanel, BorderLayout.CENTER);

        btnStartQuiz.addActionListener(e -> {
            dispose();
            new ClassSelectionPage(Main.CURRENT_USER).setVisible(true);
        });

        btnViewHistory.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur history nilai akan segera hadir!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        btnLogout.addActionListener(e -> {
            Main.CURRENT_USER = null;
            dispose();
            new HomePage().setVisible(true);
        });
    }
}