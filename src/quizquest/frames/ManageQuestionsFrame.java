// File: quizquest.frames.ManageQuestionsFrame.java
package quizquest.frames;

import quizquest.model.DatabaseConnection;
import quizquest.view.AdminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManageQuestionsFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtQuestion, txtOptA, txtOptB, txtOptC, txtOptD;
    private JComboBox<String> comboClass, comboLevel, comboCorrect;
    private JButton btnAdd, btnUpdate, btnDelete, btnFilter, btnClear, btnBack;
    private JTextField txtImagePath;
    private JButton btnBrowseImage;
    private byte[] currentImageData = null; // simpan byte array gambar
    private int selectedQuestionId = -1;

    public ManageQuestionsFrame() {
        setTitle("Kelola Soal Kuis - Admin");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // tabel model pertanyaan
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Pertanyaan", "A", "B", "C", "D", "Jawaban", "Kelas", "Level", "Gambar"}, 0
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

        // Form input
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Soal"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Pertanyaan
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Pertanyaan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtQuestion = new JTextField(30);
        formPanel.add(txtQuestion, gbc);

        // Gambar
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Gambar:"), gbc);
        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout());
        btnBrowseImage = new JButton("Pilih Gambar");
        btnBrowseImage.addActionListener(e -> browseImage());
        imagePanel.add(btnBrowseImage, BorderLayout.CENTER);
        formPanel.add(imagePanel, gbc);
        
        // Opsi A
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Opsi A:"), gbc);
        gbc.gridx = 1;
        txtOptA = new JTextField(20);
        formPanel.add(txtOptA, gbc);

        // Opsi B
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Opsi B:"), gbc);
        gbc.gridx = 1;
        txtOptB = new JTextField(20);
        formPanel.add(txtOptB, gbc);

        // Opsi C
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Opsi C:"), gbc);
        gbc.gridx = 1;
        txtOptC = new JTextField(20);
        formPanel.add(txtOptC, gbc);

        // Opsi D
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Opsi D:"), gbc);
        gbc.gridx = 1;
        txtOptD = new JTextField(20);
        formPanel.add(txtOptD, gbc);

        // Jawaban Benar
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Jawaban Benar:"), gbc);
        gbc.gridx = 1;
        comboCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        formPanel.add(comboCorrect, gbc);

        // Kelas
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Kelas:"), gbc);
        gbc.gridx = 1;
        comboClass = new JComboBox<>(new String[]{"7", "8", "9"});
        formPanel.add(comboClass, gbc);

        // Level
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Level:"), gbc);
        gbc.gridx = 1;
        comboLevel = new JComboBox<>();
        for (int i = 1; i <= 10; i++) {
            comboLevel.addItem(String.valueOf(i));
        }
        formPanel.add(comboLevel, gbc);

        // Tombol aksi
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnAdd = new JButton("Tambah");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Hapus");
        btnClear = new JButton("Bersihkan");
        btnBack = new JButton("Kembali");

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        // Layout utama
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        btnAdd.addActionListener(e -> addQuestion());
        btnUpdate.addActionListener(e -> updateQuestion());
        btnDelete.addActionListener(e -> deleteQuestion());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> dispose());

        // Muat semua soal
        loadAllQuestions();
    }

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
        
        // Ambil byte array dari DB
        byte[] imageData = (byte[]) table.getValueAt(row, 9); // kolom ke-9 adalah image_data
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
            (question_text, option_a, option_b, option_c, option_d, correct_option, class_level, quiz_level, image_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            stmt.setBytes(9, currentImageData);
            
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
            correct_option = ?, class_level = ?, quiz_level = ?, image_data = ?
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
            stmt.setBytes(9, currentImageData);
            stmt.setInt(9, selectedQuestionId);

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
        selectedQuestionId = -1;
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
}