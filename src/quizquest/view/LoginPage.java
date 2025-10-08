// File: quizquest.view.LoginPage.java
package quizquest.view;

import quizquest.Main;
import quizquest.model.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginPage() {
        setTitle("Login - Quiz Quest");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(4, 1, 10, 10));

        add(new JLabel("Username:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("Login");
        add(btnLogin);

        btnLogin.addActionListener(e -> validateLogin());
    }

    private void validateLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT username, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbUsername = rs.getString("username");
                    String role = rs.getString("role");

                    // ✅ SIMPAN USERNAME DARI DATABASE KE SESSION
                    Main.CURRENT_USER = dbUsername;

                    JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + role + "!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                    if ("admin".equals(role)) {
                        new AdminDashboard().setVisible(true);
                    } else if ("siswa".equals(role)) {
                        new UserHomePage().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Role tidak dikenali!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Username atau password salah!", "Gagal Login", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error koneksi database:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}