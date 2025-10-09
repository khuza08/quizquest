// File: quizquest/view/ClassSelectionPage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClassSelectionPage extends JFrame {
    private String username; // null jika tidak login
    private JButton btnClass7, btnClass8, btnClass9, btnBack;

    public ClassSelectionPage(String username) {
        this.username = username;
        setTitle("Pilih Kelas");
        setSize(300, 230); // sedikit diperbesar untuk muat tombol kembali
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(5, 1, 10, 10)); // ubah jadi 5 baris

        add(new JLabel("Pilih Kelas:", JLabel.CENTER));

        btnClass7 = new JButton("Kelas 7");
        btnClass8 = new JButton("Kelas 8");
        btnClass9 = new JButton("Kelas 9");
        btnBack = new JButton("Kembali");

        add(btnClass7);
        add(btnClass8);
        add(btnClass9);
        add(btnBack);

        btnClass7.addActionListener(e -> openLevelSelection(7));
        btnClass8.addActionListener(e -> openLevelSelection(8));
        btnClass9.addActionListener(e -> openLevelSelection(9));
        btnBack.addActionListener(e -> {
            dispose();
            if (username != null && !username.isEmpty()) {
                new UserHomePage().setVisible(true);
            } else {
                new HomePage().setVisible(true);
            }
        });

        // === Key bindings ===
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getRootPane().getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnBack.doClick();
            }
        });
    }

    private void openLevelSelection(int className) {
        new LevelSelectionPage(className, username).setVisible(true);
        dispose();
    }
}