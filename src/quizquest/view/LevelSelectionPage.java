// File: quizquest.view.LevelSelectionPage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LevelSelectionPage extends JFrame {
    private int className;
    private String username; // null jika tidak login

    public LevelSelectionPage(int className, String username) {
        this.className = className;
        this.username = username;

        setTitle("Kelas " + className + " - Pilih Level");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(11, 1, 5, 5));
        add(new JLabel("Pilih Level:", JLabel.CENTER));

        for (int i = 1; i <= 10; i++) {
            JButton btnLevel = new JButton("Level " + i);
            final int level = i;
            btnLevel.addActionListener(e -> startQuiz(level));
            add(btnLevel);
        }
    }

    private void startQuiz(int level) {
        new QuizGameFrame(className, level, username).setVisible(true);
        dispose();
    }
}