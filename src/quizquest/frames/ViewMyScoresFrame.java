// quizquest.frames.ViewMyScoresFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.UserHomePage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewMyScoresFrame extends JFrame {
    private final String username;
    private JTable table;
    private DefaultTableModel tableModel;

    public ViewMyScoresFrame(String username) {
        this.username = username;
        setTitle("Riwayat Nilai Saya - Quiz Quest");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Kelas", "Level", "Benar", "Salah", "Nilai", "Tanggal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnBack = new JButton("Kembali");
        btnBack.addActionListener(e -> {
            dispose();
            new UserHomePage().setVisible(true);
        });
        add(btnBack, BorderLayout.SOUTH);

        loadScores();
    }

    private void loadScores() {
        tableModel.setRowCount(0);
        String sql = """
            SELECT id, class_level, quiz_level, score, total_questions, created_at
            FROM scores
            WHERE username = ?
            ORDER BY created_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int score = rs.getInt("score");
                int total = rs.getInt("total_questions");
                int salah = total - score;
                int nilai = 100 - (salah * 5);

                Object[] row = {
                    rs.getInt("id"),
                    rs.getInt("class_level"),
                    rs.getInt("quiz_level"),
                    score,
                    salah,
                    nilai,
                    rs.getTimestamp("created_at").toString().substring(0, 19)
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat nilai.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}