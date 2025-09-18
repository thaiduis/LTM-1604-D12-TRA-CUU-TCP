package com.dictionary.server;

import com.dictionary.model.Word;
import com.dictionary.database.DictionaryDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Giao diện GUI quản trị cho từ điển Anh-Việt
 */
public class DictionaryServerGUI extends JFrame {
    private DictionaryDAO dictionaryDAO;
    private JTable wordTable;
    private DefaultTableModel tableModel;
    
    private JTextField englishField;
    private JComboBox<String> partOfSpeechField;
    private JTextField phoneticField;
    private JTextField vietnameseField;
    private JTextArea definitionArea;
    private JTextField exampleField;

    public DictionaryServerGUI() {
        this.dictionaryDAO = new DictionaryDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshWordList();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Từ điển Anh-Việt - Quản trị");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Khởi tạo table model
        String[] columnNames = {"Từ tiếng Anh", "Từ loại", "Phiên âm", "Nghĩa tiếng Việt", "Định nghĩa chi tiết", "Ví dụ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Điều chỉnh độ rộng cột
        wordTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        wordTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        wordTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        wordTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        wordTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        wordTable.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Khởi tạo các trường nhập liệu
        englishField = new JTextField(15);
        String[] partsOfSpeech = {"noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "exclamation"};
        partOfSpeechField = new JComboBox<>(partsOfSpeech);
        phoneticField = new JTextField(15);
        vietnameseField = new JTextField(15);
        definitionArea = new JTextArea(3, 30);
        definitionArea.setLineWrap(true);
        definitionArea.setWrapStyleWord(true);
        exampleField = new JTextField(30);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel bảng từ điển
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách từ trong từ điển"));
        JScrollPane scrollPane = new JScrollPane(wordTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Panel nhập liệu
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thêm/Sửa từ"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Thêm các trường nhập liệu
        addFormField(inputPanel, "Từ tiếng Anh:", englishField, gbc, 0);
        addFormField(inputPanel, "Từ loại:", partOfSpeechField, gbc, 1);
        addFormField(inputPanel, "Phiên âm:", phoneticField, gbc, 2);
        addFormField(inputPanel, "Nghĩa tiếng Việt:", vietnameseField, gbc, 3);
        
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Định nghĩa chi tiết:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(new JScrollPane(definitionArea), gbc);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Ví dụ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(exampleField, gbc);

        // Panel nút điều khiển
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Thêm từ mới");
        addButton.addActionListener(e -> addWord());
        JButton updateButton = new JButton("Cập nhật từ");
        updateButton.addActionListener(e -> updateWord());
        JButton deleteButton = new JButton("Xóa từ");
        deleteButton.addActionListener(e -> deleteWord());
        JButton clearButton = new JButton("Xóa trường");
        clearButton.addActionListener(e -> clearFields());
        JButton refreshButton = new JButton("Làm mới danh sách");
        refreshButton.addActionListener(e -> refreshWordList());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3;
        inputPanel.add(buttonPanel, gbc);

        // Thêm panel nhập liệu vào frame
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void setupEventHandlers() {
        wordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = wordTable.getSelectedRow();
                if (selectedRow >= 0) {
                    englishField.setText((String) tableModel.getValueAt(selectedRow, 0));
                    partOfSpeechField.setSelectedItem(tableModel.getValueAt(selectedRow, 1));
                    phoneticField.setText((String) tableModel.getValueAt(selectedRow, 2));
                    vietnameseField.setText((String) tableModel.getValueAt(selectedRow, 3));
                    definitionArea.setText((String) tableModel.getValueAt(selectedRow, 4));
                    exampleField.setText((String) tableModel.getValueAt(selectedRow, 5));
                }
            }
        });
    }

    private Word createWordFromFields() {
        return new Word(
            englishField.getText().trim(),
            (String) partOfSpeechField.getSelectedItem(),
            phoneticField.getText().trim(),
            vietnameseField.getText().trim(),
            definitionArea.getText().trim(),
            exampleField.getText().trim()
        );
    }

    private void addWord() {
        if (!validateFields()) return;

        Word word = createWordFromFields();
        boolean success = dictionaryDAO.addWord(word);

        if (success) {
            JOptionPane.showMessageDialog(this, "Thêm từ thành công!");
            clearFields();
            refreshWordList();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể thêm từ. Có thể từ này đã tồn tại.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateWord() {
        if (!validateFields()) return;

        Word word = createWordFromFields();
        boolean success = dictionaryDAO.updateWord(word);

        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật từ thành công!");
            refreshWordList();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật từ. Vui lòng thử lại.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteWord() {
        String english = englishField.getText().trim();
        String partOfSpeech = (String) partOfSpeechField.getSelectedItem();

        if (english.isEmpty() || partOfSpeech == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn từ cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa từ '" + english + "' (" + partOfSpeech + ")?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dictionaryDAO.deleteWord(english, partOfSpeech);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa từ thành công!");
                clearFields();
                refreshWordList();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa từ. Vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateFields() {
        if (englishField.getText().trim().isEmpty() ||
            vietnameseField.getText().trim().isEmpty() ||
            phoneticField.getText().trim().isEmpty() ||
            definitionArea.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ thông tin!\n" +
                "Các trường bắt buộc: từ tiếng Anh, từ loại, phiên âm, nghĩa tiếng Việt, định nghĩa chi tiết",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearFields() {
        englishField.setText("");
        partOfSpeechField.setSelectedIndex(0);
        phoneticField.setText("");
        vietnameseField.setText("");
        definitionArea.setText("");
        exampleField.setText("");
        wordTable.clearSelection();
    }

    private void refreshWordList() {
        List<Word> words = dictionaryDAO.getAllWords();
        tableModel.setRowCount(0);
        
        for (Word word : words) {
            Object[] row = {
                word.getEnglishWord(),
                word.getPartOfSpeech(),
                word.getPhoneticSpelling(),
                word.getVietnameseMeaning(),
                word.getDetailedDefinition(),
                word.getExampleSentence()
            };
            tableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DictionaryServerGUI();
        });
    }
}