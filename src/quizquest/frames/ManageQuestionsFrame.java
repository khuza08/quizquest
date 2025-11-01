// File: quizquest/frames/ManageQuestionsFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.AdminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class ManageQuestionsFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtQuestion, txtOptA, txtOptB, txtOptC, txtOptD;
    private JComboBox<String> comboClass, comboLevel, comboCorrect, comboCategory;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnBack;
    private JButton btnBrowseImage;
    private byte[] currentImageData = null;
    private int selectedQuestionId = -1;

    private static final List<String> CATEGORIES = Arrays.asList(
        "Umum", "Tumbuhan", "Hewan", "Negara", "Sejarah", "Sains", "Matematika", "Bahasa"
    );

    private Point initialClick;

    public ManageQuestionsFrame() {
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

        // === Content Panel (Table + Form) ===
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Table (scrollable with fixed height)
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Pertanyaan", "A", "B", "C", "D", "Jawaban", "Kelas", "Level", "Kategori", "Gambar"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    loadSelectedQuestionToForm(row);
                }
            }
        });

        // Wrap table in scroll pane with fixed height
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(0, 200)); // ← Fixed height for scrollable area
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200)); // Prevent vertical expansion

        contentPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Soal"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Pertanyaan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtQuestion = new JTextField(30);
        formPanel.add(txtQuestion, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Opsi A:"), gbc);
        gbc.gridx = 1;
        txtOptA = new JTextField(20);
        formPanel.add(txtOptA, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Opsi B:"), gbc);
        gbc.gridx = 1;
        txtOptB = new JTextField(20);
        formPanel.add(txtOptB, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Opsi C:"), gbc);
        gbc.gridx = 1;
        txtOptC = new JTextField(20);
        formPanel.add(txtOptC, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Opsi D:"), gbc);
        gbc.gridx = 1;
        txtOptD = new JTextField(20);
        formPanel.add(txtOptD, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Jawaban Benar:"), gbc);
        gbc.gridx = 1;
        comboCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        formPanel.add(comboCorrect, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Kelas:"), gbc);
        gbc.gridx = 1;
        comboClass = new JComboBox<>(new String[]{"7", "8", "9"});
        formPanel.add(comboClass, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Level:"), gbc);
        gbc.gridx = 1;
        comboLevel = new JComboBox<>();
        for (int i = 1; i <= 10; i++) {
            comboLevel.addItem(String.valueOf(i));
        }
        formPanel.add(comboLevel, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1;
        comboCategory = new JComboBox<>(CATEGORIES.toArray(new String[0]));
        formPanel.add(comboCategory, gbc);

        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Gambar:"), gbc);
        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout());
        btnBrowseImage = new JButton("Pilih Gambar");
        btnBrowseImage.addActionListener(e -> browseImage());
        imagePanel.add(btnBrowseImage, BorderLayout.CENTER);
        formPanel.add(imagePanel, gbc);

        contentPanel.add(formPanel, BorderLayout.NORTH);

        // === Button Panel (Single Row - Columns) ===
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 0)); // 1 row, 5 columns
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        btnAdd = createStyledButton("Tambah", 50);
        btnUpdate = createStyledButton("Update", 50);
        btnDelete = createStyledButton("Hapus", 50);
        btnClear = createStyledButton("Bersihkan", 50);
        btnBack = createStyledButton("Kembali", 50);

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        // === Assembly ===
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        // Event handlers
        btnAdd.addActionListener(e -> addQuestion());
        btnUpdate.addActionListener(e -> updateQuestion());
        btnDelete.addActionListener(e -> deleteQuestion());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> dispose());

        // Draggable
        makeDraggable(titleBar);
        makeDraggable(contentPanel);

        loadAllQuestions();

        // ✅ Set minimum window size (width x height) before packing
        setMinimumSize(new Dimension(1000, 600)); // ← Adjust width as needed

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

    private void loadAllQuestions() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM questions ORDER BY class_level, quiz_level, id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option"),
                    rs.getInt("class_level"),
                    rs.getInt("quiz_level"),
                    rs.getString("category"),
                    rs.getBytes("image_data")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat soal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedQuestionToForm(int row) {
        selectedQuestionId = (int) table.getValueAt(row, 0);
        txtQuestion.setText((String) table.getValueAt(row, 1));
        txtOptA.setText((String) table.getValueAt(row, 2));
        txtOptB.setText((String) table.getValueAt(row, 3));
        txtOptC.setText((String) table.getValueAt(row, 4));
        txtOptD.setText((String) table.getValueAt(row, 5));
        comboCorrect.setSelectedItem(table.getValueAt(row, 6));
        comboClass.setSelectedItem(String.valueOf(table.getValueAt(row, 7)));
        comboLevel.setSelectedItem(String.valueOf(table.getValueAt(row, 8)));
        comboCategory.setSelectedItem(table.getValueAt(row, 9));

        byte[] imageData = (byte[]) table.getValueAt(row, 10);
        if (imageData != null && imageData.length > 0) {
            currentImageData = imageData;
            btnBrowseImage.setText("✅ Gambar tersedia");
        } else {
            currentImageData = null;
            btnBrowseImage.setText("Pilih Gambar");
        }

        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void addQuestion() {
        if (!validateInputs()) return;

        String sql = """
            INSERT INTO questions 
            (question_text, option_a, option_b, option_c, option_d, correct_option, 
             class_level, quiz_level, category, image_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtQuestion.getText().trim());
            stmt.setString(2, txtOptA.getText().trim());
            stmt.setString(3, txtOptB.getText().trim());
            stmt.setString(4, txtOptC.getText().trim());
            stmt.setString(5, txtOptD.getText().trim());
            stmt.setString(6, (String) comboCorrect.getSelectedItem());
            stmt.setInt(7, Integer.parseInt((String) comboClass.getSelectedItem()));
            stmt.setInt(8, Integer.parseInt((String) comboLevel.getSelectedItem()));
            stmt.setString(9, (String) comboCategory.getSelectedItem());
            stmt.setBytes(10, currentImageData);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Soal berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambah soal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQuestion() {
        if (selectedQuestionId == -1 || !validateInputs()) return;

        String sql = """
            UPDATE questions SET
            question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?,
            correct_option = ?, class_level = ?, quiz_level = ?, category = ?, image_data = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtQuestion.getText().trim());
            stmt.setString(2, txtOptA.getText().trim());
            stmt.setString(3, txtOptB.getText().trim());
            stmt.setString(4, txtOptC.getText().trim());
            stmt.setString(5, txtOptD.getText().trim());
            stmt.setString(6, (String) comboCorrect.getSelectedItem());
            stmt.setInt(7, Integer.parseInt((String) comboClass.getSelectedItem()));
            stmt.setInt(8, Integer.parseInt((String) comboLevel.getSelectedItem()));
            stmt.setString(9, (String) comboCategory.getSelectedItem());
            stmt.setBytes(10, currentImageData);
            stmt.setInt(11, selectedQuestionId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Soal berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadAllQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal update soal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteQuestion() {
        if (selectedQuestionId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin hapus soal ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM questions WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, selectedQuestionId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Soal berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAllQuestions();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal hapus soal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void browseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Pilih Gambar");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = chooser.getSelectedFile();
                currentImageData = java.nio.file.Files.readAllBytes(file.toPath());
                btnBrowseImage.setText("✅ Gambar dipilih");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal baca gambar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                currentImageData = null;
                btnBrowseImage.setText("Pilih Gambar");
            }
        }
    }

    private boolean validateInputs() {
        if (txtQuestion.getText().trim().isEmpty()) {
            showError("Pertanyaan tidak boleh kosong!");
            return false;
        }
        if (txtOptA.getText().trim().isEmpty() || txtOptB.getText().trim().isEmpty() ||
            txtOptC.getText().trim().isEmpty() || txtOptD.getText().trim().isEmpty()) {
            showError("Semua opsi harus diisi!");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
    }

    private void clearForm() {
        txtQuestion.setText("");
        txtOptA.setText("");
        txtOptB.setText("");
        txtOptC.setText("");
        txtOptD.setText("");
        comboCorrect.setSelectedIndex(0);
        comboClass.setSelectedIndex(0);
        comboLevel.setSelectedIndex(0);
        comboCategory.setSelectedIndex(0);
        currentImageData = null;
        btnBrowseImage.setText("Pilih Gambar");
        selectedQuestionId = -1;
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
}           