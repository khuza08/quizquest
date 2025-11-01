// File: quizquest.frames.ViewScoresFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.AdminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ViewScoresFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboClassFilter, comboLevelFilter;
    private JButton btnFilter, btnRefresh, btnBack;

    private Point initialClick;

    public ViewScoresFrame() {
        // ✅ Initialize FlatLaf (like in HomePage)
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setUndecorated(true); // ✅ Remove native title bar
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0)); // Transparent background for rounded corners

        // Main rounded panel
        JPanel mainPanel = new RoundedPanel(32);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));

        // === macOS Title Bar ===
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 40));
        titleBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dotsPanel.setOpaque(false);

        JButton redDot = createMacOSDot(new Color(0xFF5F57), "Close");
        JButton yellowDot = createMacOSDot(new Color(0xFFBD2E), "Minimize");
        JButton greenDot = createMacOSDot(new Color(0x28CA42), "Maximize");

        dotsPanel.add(redDot);
        dotsPanel.add(yellowDot);
        dotsPanel.add(greenDot);
        titleBar.add(dotsPanel, BorderLayout.WEST);

        // === Content Panel (Table + Filter) ===
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Model tabel
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Siswa", "Kelas", "Level", "Benar", "Salah", "Nilai", "Tanggal"}, 0 // ← ganti "Skor", "Total" jadi "Nilai"
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // biar bisa sort klik header

        // Wrap table in scroll pane with fixed height
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(0, 300)); // ← Fixed height for scrollable area
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300)); // Prevent vertical expansion

        contentPanel.add(tableScrollPane, BorderLayout.CENTER);

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

        btnFilter = createStyledButton("Terapkan Filter", 40);
        btnRefresh = createStyledButton("Refresh", 40);
        btnBack = createStyledButton("Kembali", 40);

        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);
        filterPanel.add(btnBack);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // === Button Panel (Single Row - Columns) ===
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 1 row, 3 columns
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        buttonPanel.add(btnFilter);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);

        // === Assembly ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        // Event handlers
        btnFilter.addActionListener(e -> loadScoresWithFilter());
        btnRefresh.addActionListener(e -> loadAllScores());
        btnBack.addActionListener(e -> dispose());

        // Draggable
        makeDraggable(titleBar);
        makeDraggable(contentPanel);

        // Muat semua data awal
        loadAllScores();

        // ✅ Set minimum window size (width x height) before packing
        setMinimumSize(new Dimension(800, 550)); // ← Adjust width as needed

        // ✅ Call pack() to auto-size window to fit content
        pack(); // ← This is what makes it NOT too tall!
    }

    // === Helper Methods (same as HomePage) ===

    private JButton createMacOSDot(Color color, String action) {
        JButton dot = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        dot.setPreferredSize(new Dimension(14, 14));
        dot.setMaximumSize(new Dimension(14, 14));
        dot.setContentAreaFilled(false);
        dot.setBorderPainted(false);
        dot.setFocusPainted(false);
        dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if ("Close".equals(action)) {
            dot.addActionListener(e -> dispose());
        } else if ("Minimize".equals(action)) {
            dot.addActionListener(e -> setState(JFrame.ICONIFIED));
        }

        return dot;
    }

    private JButton createStyledButton(String text, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0x2D2D2D));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, height));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0x404040));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0x2D2D2D));
            }
        });

        return btn;
    }

    private void makeDraggable(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        comp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    setLocation(e.getXOnScreen() - initialClick.x, e.getYOnScreen() - initialClick.y);
                }
            }
        });
    }

    private class RoundedPanel extends JPanel {
        private int radius;
        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // === Original Logic (unchanged) ===

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
            "SELECT id, username, class_level, quiz_level, score, total_questions, created_at " +
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
                
                int score = rs.getInt("score");        // jumlah benar
                int total = rs.getInt("total_questions");
                int salah = total - score;             // hitung salah
                int nilai = 100 - (salah * 5);         // setiap salah -5 dari 100

                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("class_level"),
                    rs.getInt("quiz_level"),
                    score,          // Benar
                    salah,          // Salah
                    nilai,          // Nilai skala 100
                    rs.getTimestamp("created_at").toString().substring(0, 19)
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat nilai: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}