// File: quizquest.view/LoginPage.java
package quizquest.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if ("admin".equals(username) && "admin123".equals(password)) {
            JOptionPane.showMessageDialog(this, "Login sebagai Admin!");
            dispose();
            new AdminDashboard().setVisible(true);
        } else if ("user".equals(username) && "user123".equals(password)) {
            JOptionPane.showMessageDialog(this, "Login sebagai User!");
            dispose();
            new UserHomePage().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Username atau password salah!");
        }
    }
}