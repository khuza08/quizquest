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

        // Header: tampilkan username
        String username = Main.CURRENT_USER != null ? Main.CURRENT_USER : "Siswa";
        JLabel header = new JLabel("Halo, " + username + "!", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(header, BorderLayout.NORTH);

        // Panel tombol
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnStartQuiz = new JButton("Mulai Kuis");
        JButton btnViewHistory = new JButton("Lihat History Nilai");
        JButton btnLogout = new JButton("Logout");

        buttonPanel.add(btnStartQuiz);
        buttonPanel.add(btnViewHistory);
        buttonPanel.add(btnLogout);

        add(buttonPanel, BorderLayout.CENTER);

        // Event handlers
        btnStartQuiz.addActionListener(e -> {
            dispose();
            new ClassSelectionPage(Main.CURRENT_USER).setVisible(true); // kirim username
        });

        btnViewHistory.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur history nilai akan segera hadir!", "Info", JOptionPane.INFORMATION_MESSAGE);
            // Nanti ganti dengan: new ViewUserScoresFrame().setVisible(true);
        });

        btnLogout.addActionListener(e -> {
            Main.CURRENT_USER = null; // Hapus sesi
            dispose();
            new HomePage().setVisible(true);
        });
    }
}