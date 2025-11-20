package com.dictionary.server;

import com.dictionary.database.DictionaryDAO;
import com.dictionary.model.Word;
import com.dictionary.ui.UIUtils;

import javax.swing.*;
import java.awt.*;

public class DictionaryFormDialog extends JDialog {
    private DictionaryDAO dictionaryDAO;
    private DictionaryServerGUI parentGUI;
    
    private JTextField englishField;
    private JComboBox<String> partOfSpeechField;
    private JTextField phoneticField;
    private JTextField vietnameseField;
    private JTextArea definitionArea;
    private JTextField exampleField;
    private JTextField imagePathField;
    private JButton browseImageButton;
    
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton cancelButton;
    private JButton importButton;
    
    private String currentMode = "add"; // "add", "update", "delete"
    

    public DictionaryFormDialog(DictionaryServerGUI parent, DictionaryDAO dao) {
        super(parent, "Quản lý từ điển", true);
        this.parentGUI = parent;
        this.dictionaryDAO = dao;
        
        UIUtils.setUIStyle();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        setSize(650, 600);
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);

        englishField = new JTextField(20);
        englishField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        englishField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        String[] partsOfSpeech = {"noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "exclamation"};
        partOfSpeechField = new JComboBox<>(partsOfSpeech);
        partOfSpeechField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        phoneticField = new JTextField(20);
        phoneticField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        phoneticField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        vietnameseField = new JTextField(20);
        vietnameseField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        vietnameseField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        definitionArea = new JTextArea(4, 30);
        definitionArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        definitionArea.setLineWrap(true);
        definitionArea.setWrapStyleWord(true);
        definitionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        exampleField = new JTextField(30);
        exampleField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        exampleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Ảnh minh họa
        imagePathField = new JTextField(25);
        imagePathField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        imagePathField.setEditable(false);
        imagePathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        browseImageButton = UIUtils.createStyledButton("Chọn ảnh", new Color(100, 181, 246), new Color(66, 165, 245));

        addButton = UIUtils.createStyledButton("Thêm từ mới", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        updateButton = UIUtils.createStyledButton("Cập nhật", UIUtils.PRIMARY_COLOR, new Color(20, 70, 220));
        deleteButton = UIUtils.createStyledButton("Xóa từ", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));
        importButton = UIUtils.createStyledButton("Import từ file", new Color(156, 39, 176), new Color(123, 31, 162));
        cancelButton = UIUtils.createStyledButton("Hủy", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel("QUẢN LÝ TỪ ĐIỂN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(formPanel, "Từ tiếng Anh:", englishField, gbc, 0);
        addFormField(formPanel, "Từ loại:", partOfSpeechField, gbc, 1);
        addFormField(formPanel, "Phiên âm:", phoneticField, gbc, 2);
        addFormField(formPanel, "Nghĩa tiếng Việt:", vietnameseField, gbc, 3);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel defLabel = new JLabel("Định nghĩa chi tiết:");
        defLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(defLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(definitionArea), gbc);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel exLabel = new JLabel("Ví dụ:");
        exLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(exLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(exampleField, gbc);

        // Thêm trường ảnh minh họa
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel imgLabel = new JLabel("Ảnh minh họa:");
        imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(imgLabel, gbc);
        
        JPanel imagePanel = new JPanel(new BorderLayout(8, 0));
        imagePanel.setOpaque(false);
        imagePanel.add(imagePathField, BorderLayout.CENTER);
        imagePanel.add(browseImageButton, BorderLayout.EAST);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(imagePanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3;
        formPanel.add(buttonPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

        updateButtonVisibility();
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(fieldLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addWord());
        updateButton.addActionListener(e -> updateWord());
        deleteButton.addActionListener(e -> deleteWord());
        cancelButton.addActionListener(e -> dispose());
        importButton.addActionListener(e -> importWordsFromFile());
        browseImageButton.addActionListener(e -> browseForImage());
    }

    private void browseForImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh minh họa");
        
        // Chỉ cho phép chọn file ảnh
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                       name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
            }
            
            @Override
            public String getDescription() {
                return "Ảnh (*.jpg, *.jpeg, *.png, *.gif, *.bmp)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }
    private void importWordsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file dữ liệu (CSV)");
        int result = fileChooser.showOpenDialog(this);
    
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            int count = 0;
    
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // bỏ qua dòng trống hoặc header
                    if (line.trim().isEmpty() || line.startsWith("english")) continue;
    
                    // tách theo dấu phẩy
                    String[] parts = line.split(",", -1);
                    if (parts.length < 6) continue; // không đủ dữ liệu
    
                    Word word = new Word(
                        parts[0].trim(), // english
                        parts[1].trim(), // part of speech
                        parts[2].trim(), // phonetic
                        parts[3].trim(), // vietnamese
                        parts[4].trim(), // definition
                        parts[5].trim()  // example
                    );
    
                    if (dictionaryDAO.addWord(word)) {
                        count++;
                        parentGUI.logActivity("Import", "Đã import: " + word.getEnglishWord());
                    }
                }
    
                JOptionPane.showMessageDialog(this, "Import thành công " + count + " từ!");
                parentGUI.refreshWordList();
    
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    public void showForAdd() {
        currentMode = "add";
        setTitle("Thêm từ mới");
        clearFields();
        updateButtonVisibility();
        setVisible(true);
    }

    public void showForUpdate(Word word) {
        currentMode = "update";
        setTitle("Cập nhật từ");
        fillFields(word);
        updateButtonVisibility();
        setVisible(true);
    }

    public void showForDelete(Word word) {
        currentMode = "delete";
        setTitle("Xóa từ");
        fillFields(word);
        setFieldsEditable(false);
        updateButtonVisibility();
        setVisible(true);
    }

    private void updateButtonVisibility() {
        addButton.setVisible("add".equals(currentMode));
        updateButton.setVisible("update".equals(currentMode));
        deleteButton.setVisible("delete".equals(currentMode));
        importButton.setVisible("add".equals(currentMode)); // Chỉ hiển thị ở chế độ thêm từ mới
        cancelButton.setVisible(true);
    }

    private void fillFields(Word word) {
        englishField.setText(word.getEnglishWord());
        partOfSpeechField.setSelectedItem(word.getPartOfSpeech());
        phoneticField.setText(word.getPhoneticSpelling());
        vietnameseField.setText(word.getVietnameseMeaning());
        definitionArea.setText(word.getDetailedDefinition());
        exampleField.setText(word.getExampleSentence());
        imagePathField.setText(word.getImagePath() != null ? word.getImagePath() : "");
    }

    private void setFieldsEditable(boolean editable) {
        englishField.setEditable(editable);
        partOfSpeechField.setEnabled(editable);
        phoneticField.setEditable(editable);
        vietnameseField.setEditable(editable);
        definitionArea.setEditable(editable);
        exampleField.setEditable(editable);
        browseImageButton.setEnabled(editable);
    }

    private Word createWordFromFields() {
        return new Word(
            englishField.getText().trim(),
            (String) partOfSpeechField.getSelectedItem(),
            phoneticField.getText().trim(),
            vietnameseField.getText().trim(),
            definitionArea.getText().trim(),
            exampleField.getText().trim(),
            imagePathField.getText().trim().isEmpty() ? null : imagePathField.getText().trim()
        );
    }

    private void addWord() {
        if (!validateFields()) return;
        Word word = createWordFromFields();
        if (dictionaryDAO.addWord(word)) {
            JOptionPane.showMessageDialog(this, "Thêm từ thành công!");
            parentGUI.refreshWordList();
            parentGUI.logActivity("Thêm từ", "Đã thêm: " + word.getEnglishWord()); // ✅ log khi thành công
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể thêm từ. Có thể từ này đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateWord() {
        if (!validateFields()) return;
        Word word = createWordFromFields();
        if (dictionaryDAO.updateWord(word)) {
            JOptionPane.showMessageDialog(this, "Cập nhật từ thành công!");
            parentGUI.refreshWordList();
            parentGUI.logActivity("Cập nhật từ", "Đã cập nhật: " + word.getEnglishWord()); // ✅ log khi thành công
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật từ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteWord() {
        String english = englishField.getText().trim();
        String partOfSpeech = (String) partOfSpeechField.getSelectedItem();
        if (english.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn từ cần xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa từ '" + english + "' (" + partOfSpeech + ")?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dictionaryDAO.deleteWord(english, partOfSpeech)) {
                JOptionPane.showMessageDialog(this, "Xóa từ thành công!");
                parentGUI.refreshWordList();
                parentGUI.logActivity("Xóa từ", "Đã xóa: " + english);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa từ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateFields() {
        return !(englishField.getText().trim().isEmpty()
            || vietnameseField.getText().trim().isEmpty()
            || phoneticField.getText().trim().isEmpty()
            || definitionArea.getText().trim().isEmpty());
    }

    private void clearFields() {
        englishField.setText("");
        partOfSpeechField.setSelectedIndex(0);
        phoneticField.setText("");
        vietnameseField.setText("");
        definitionArea.setText("");
        exampleField.setText("");
        imagePathField.setText("");
        setFieldsEditable(true);
    }
}
