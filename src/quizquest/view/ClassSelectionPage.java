// File: quizquest.view.ClassSelectionPage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClassSelectionPage extends JFrame {
    private String username; // null jika tidak login
    private JButton btnClass7, btnClass8, btnClass9;

    public ClassSelectionPage(String username) {
        this.username = username;
        setTitle("Pilih Kelas");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(4, 1, 10, 10));

        add(new JLabel("Pilih Kelas:", JLabel.CENTER));

        btnClass7 = new JButton("Kelas 7");
        btnClass8 = new JButton("Kelas 8");
        btnClass9 = new JButton("Kelas 9");

        add(btnClass7);
        add(btnClass8);
        add(btnClass9);

        btnClass7.addActionListener(e -> openLevelSelection(7));
        btnClass8.addActionListener(e -> openLevelSelection(8));
        btnClass9.addActionListener(e -> openLevelSelection(9));
    }

    private void openLevelSelection(int className) {
        new LevelSelectionPage(className, username).setVisible(true);
        dispose();
    }
}