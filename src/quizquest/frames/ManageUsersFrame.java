// File: quizquest.frames.ManageUsersFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.AdminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManageUsersFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtUsername;
    private JPasswordField txtPassword; // ✅ DIPERBAIKI: bukan JTextField!
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh, btnBack;
    private int selectedUserId = -1;

    public ManageUsersFrame() {
        setTitle("Kelola Siswa - Admin");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Model tabel
        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Password", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel hanya baca
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedUserId = (int) table.getValueAt(selectedRow, 0);
                    String username = (String) table.getValueAt(selectedRow, 1);
                    String password = (String) table.getValueAt(selectedRow, 2);
                    txtUsername.setText(username);
                    txtPassword.setText(password); // setText() tetap bisa dipakai
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                }
            }
        });

        // Input form
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Siswa"));

        inputPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        inputPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField(); // ✅ Sudah benar di sini
        inputPanel.add(txtPassword);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnAdd = new JButton("Tambah");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Hapus");
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("Kembali");

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);

        // Layout utama
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnRefresh.addActionListener(e -> loadUsers());
        btnBack.addActionListener(e -> {
            dispose();
            new AdminDashboard().setVisible(true);
        });

        // Muat data awal
        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0); // Kosongkan tabel
        String sql = "SELECT id, username, password, role FROM users WHERE role = 'siswa'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                tableModel.addRow(new Object[]{id, username, password, role});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim(); // ✅ Sekarang aman

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'siswa')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Siswa berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadUsers();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                JOptionPane.showMessageDialog(this, "Username sudah digunakan!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menambah siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) return;

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim(); // ✅ Aman

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, selectedUserId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data siswa berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadUsers();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal update siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin hapus siswa ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, selectedUserId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Siswa berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal hapus siswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText(""); // Bisa pakai setText("") untuk reset
        selectedUserId = -1;
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
}