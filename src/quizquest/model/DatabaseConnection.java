// File: quizquest.model.DatabaseConnection.java
package quizquest.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Ganti USER dan PASSWORD sesuai MySQL-mu
    private static final String URL = "jdbc:mysql://localhost:3306/quizquest?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root"; // ← GANTI JIKA PERLU
    private static final String PASSWORD = "Asifppnk08"; // ← GANTI JIKA ADA PASSWORD

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver tidak ditemukan!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}