// quizquest.view.AdminDashboard.java
package quizquest.view;

import quizquest.frames.ManageQuestionsFrame;
import quizquest.frames.ManageUsersFrame;
import quizquest.frames.ViewScoresFrame;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Admin Dashboard - Quiz Quest");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(4, 1, 10, 10));

        JButton btnManageUsers = new JButton("Kelola Siswa");
        JButton btnManageQuestions = new JButton("Kelola Soal");
        JButton btnViewScores = new JButton("Lihat Nilai Siswa");
        JButton btnLogout = new JButton("Logout");

        add(btnManageUsers);
        add(btnManageQuestions);
        add(btnViewScores);
        add(btnLogout);

        // Event handlers
        btnManageUsers.addActionListener(e -> new ManageUsersFrame().setVisible(true));
        btnManageQuestions.addActionListener(e -> new ManageQuestionsFrame().setVisible(true));
        btnViewScores.addActionListener(e -> new ViewScoresFrame().setVisible(true));
        btnLogout.addActionListener(e -> {
            dispose();
            new HomePage().setVisible(true);
        });
    }
}