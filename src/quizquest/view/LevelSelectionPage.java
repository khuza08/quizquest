// File: quizquest/view/LevelSelectionPage.java
package quizquest.view;

import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class LevelSelectionPage extends JFrame {
    private int className;
    private String username; // null jika tidak login
    private JButton[] levelButtons = new JButton[10]; // simpan referensi tombol

    public LevelSelectionPage(int className, String username) {
        this.className = className;
        this.username = username;

        setTitle("Kelas " + className + " - Pilih Level");
        setSize(400, 530);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(12, 1, 5, 5));
        add(new JLabel("Pilih Level:", JLabel.CENTER));

        // Buat tombol level dulu
        for (int i = 0; i < 10; i++) {
            levelButtons[i] = new JButton("Level " + (i + 1));
            final int level = i + 1;
            levelButtons[i].addActionListener(e -> startQuiz(level));
            add(levelButtons[i]);
        }

        // Tombol Kembali
        JButton btnBack = new JButton("Kembali");
        btnBack.addActionListener(e -> {
            dispose();
            new ClassSelectionPage(username).setVisible(true);
        });
        add(btnBack);

        // Key binding ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getRootPane().getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnBack.doClick();
            }
        });

        // Cek level yang tersedia dari database
        checkAvailableLevels();
    }

    private void checkAvailableLevels() {
        String sql = """
            SELECT DISTINCT quiz_level 
            FROM questions 
            WHERE class_level = ?
            """;

        Set<Integer> availableLevels = new HashSet<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, className);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                availableLevels.add(rs.getInt("quiz_level"));
            }

            // Update tampilan tombol
            for (int i = 0; i < 10; i++) {
                int level = i + 1;
                JButton btn = levelButtons[i];
                if (!availableLevels.contains(level)) {
                    btn.setEnabled(false);
                    btn.setText("Level " + level + " (belum ada soal)");
                    // Opsional: ubah warna teks jadi abu-abu
                    btn.setForeground(Color.GRAY);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar level: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Jika error, biarkan semua tombol aktif (fallback)
        }
    }

    private void startQuiz(int level) {
        new QuizGameFrame(className, level, username).setVisible(true);
        dispose();
    }
}