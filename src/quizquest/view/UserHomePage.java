// File: quizquest.view/UserHomePage.java
package quizquest.view;

import javax.swing.*;

public class UserHomePage extends JFrame {
    public UserHomePage() {
        setTitle("User Home");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Selamat datang, User! Anda bisa mulai kuis atau lihat history.", JLabel.CENTER);
        add(label);
    }
}