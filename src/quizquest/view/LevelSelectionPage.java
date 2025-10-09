// File: quizquest/view/LevelSelectionPage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LevelSelectionPage extends JFrame {
    private int className;
    private String username; // null jika tidak login

    public LevelSelectionPage(int className, String username) {
        this.className = className;
        this.username = username;

        setTitle("Kelas " + className + " - Pilih Level");
        setSize(400, 530); // sedikit diperbesar untuk muat tombol kembali
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(12, 1, 5, 5)); // ubah jadi 12 baris (10 level + label + tombol kembali)

        add(new JLabel("Pilih Level:", JLabel.CENTER));

        for (int i = 1; i <= 10; i++) {
            JButton btnLevel = new JButton("Level " + i);
            final int level = i;
            btnLevel.addActionListener(e -> startQuiz(level));
            add(btnLevel);
        }

        // Tombol Kembali
        JButton btnBack = new JButton("Kembali");
        btnBack.addActionListener(e -> {
            dispose();
            new ClassSelectionPage(username).setVisible(true);
        });
        add(btnBack);

        // === Key binding: ESC untuk kembali ===
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getRootPane().getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnBack.doClick();
            }
        });
    }

    private void startQuiz(int level) {
        new QuizGameFrame(className, level, username).setVisible(true);
        dispose();
    }
}