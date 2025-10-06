// File: quizquest.view/AdminDashboard.java
package quizquest.view;

import javax.swing.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Dashboard Admin: Kelola Soal, Level, User", JLabel.CENTER);
        add(label);
    }
}