// File: quizquest.frames.ViewScoresFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.AdminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewScoresFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboClassFilter, comboLevelFilter;
    private JButton btnFilter, btnRefresh, btnBack;

    public ViewScoresFrame() {
        setTitle("Lihat Nilai Siswa - Admin");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Model tabel
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Siswa", "Kelas", "Level", "Skor", "Total", "Persentase", "Tanggal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // biar bisa sort klik header

        // Panel filter
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));

        filterPanel.add(new JLabel("Kelas:"));
        comboClassFilter = new JComboBox<>(new String[]{"Semua", "7", "8", "9"});
        filterPanel.add(comboClassFilter);

        filterPanel.add(new JLabel("Level:"));
        comboLevelFilter = new JComboBox<>(new String[]{"Semua"});
        for (int i = 1; i <= 10; i++) {
            comboLevelFilter.addItem(String.valueOf(i));
        }
        filterPanel.add(comboLevelFilter);

        btnFilter = new JButton("Terapkan Filter");
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("Kembali");

        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);
        filterPanel.add(btnBack);

        // Layout utama
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(filterPanel, BorderLayout.NORTH);

        // Event handlers
        btnFilter.addActionListener(e -> loadScoresWithFilter());
        btnRefresh.addActionListener(e -> loadAllScores());
        btnBack.addActionListener(e -> {
            dispose();
            new AdminDashboard().setVisible(true);
        });

        // Muat semua data awal
        loadAllScores();
    }

    private void loadAllScores() {
        loadScores(null, null);
    }

    private void loadScoresWithFilter() {
        String classFilter = null;
        String levelFilter = null;

        if (!"Semua".equals(comboClassFilter.getSelectedItem())) {
            classFilter = (String) comboClassFilter.getSelectedItem();
        }
        if (!"Semua".equals(comboLevelFilter.getSelectedItem())) {
            levelFilter = (String) comboLevelFilter.getSelectedItem();
        }

        loadScores(classFilter, levelFilter);
    }

    private void loadScores(String classFilter, String levelFilter) {
        tableModel.setRowCount(0); // Kosongkan tabel

        StringBuilder sql = new StringBuilder(
            "SELECT id, username, class_level, quiz_level, score, total_questions, percentage, created_at " +
            "FROM scores WHERE 1=1"
        );

        if (classFilter != null) {
            sql.append(" AND class_level = ").append(classFilter);
        }
        if (levelFilter != null) {
            sql.append(" AND quiz_level = ").append(levelFilter);
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("class_level"),
                    rs.getInt("quiz_level"),
                    rs.getInt("score"),
                    rs.getInt("total_questions"),
                    rs.getBigDecimal("percentage") + "%",
                    rs.getTimestamp("created_at").toString().substring(0, 19) // format: yyyy-MM-dd HH:mm:ss
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat nilai: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}