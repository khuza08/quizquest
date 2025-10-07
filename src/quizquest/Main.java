// File: quizquest/Main.java
package quizquest;

import javax.swing.SwingUtilities;
import quizquest.view.HomePage;

public class Main {
    public static String CURRENT_USER = null; // ← INI SUDAH BENAR!

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomePage().setVisible(true);
        });
    }
}