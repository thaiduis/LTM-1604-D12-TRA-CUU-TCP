package com.dictionary.server;

import com.dictionary.model.Word;
import com.dictionary.database.DictionaryDAO;
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
    
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton cancelButton;
    
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
        setSize(600, 500);
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);

        // Form fields
        englishField = new JTextField(20);
        englishField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        String[] partsOfSpeech = {"noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "exclamation"};
        partOfSpeechField = new JComboBox<>(partsOfSpeech);
        partOfSpeechField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        phoneticField = new JTextField(20);
        phoneticField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        vietnameseField = new JTextField(20);
        vietnameseField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        definitionArea = new JTextArea(4, 30);
        definitionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        definitionArea.setLineWrap(true);
        definitionArea.setWrapStyleWord(true);
        definitionArea.setBorder(UIUtils.createRoundBorder(UIUtils.DIVIDER_COLOR, 10));
        
        exampleField = new JTextField(30);
        exampleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Buttons
        addButton = UIUtils.createStyledButton("Thêm từ mới", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        updateButton = UIUtils.createStyledButton("Cập nhật", UIUtils.PRIMARY_COLOR, new Color(20, 70, 220));
        deleteButton = UIUtils.createStyledButton("Xóa từ", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));
        cancelButton = UIUtils.createStyledButton("Hủy", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0, 82, 204, 30),
                    0, getHeight(), new Color(102, 51, 153, 15)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("QUẢN LÝ TỪ ĐIỂN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UIUtils.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Form content
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(240, 242, 255)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        addFormField(formPanel, "Từ tiếng Anh:", englishField, gbc, 0);
        addFormField(formPanel, "Từ loại:", partOfSpeechField, gbc, 1);
        addFormField(formPanel, "Phiên âm:", phoneticField, gbc, 2);
        addFormField(formPanel, "Nghĩa tiếng Việt:", vietnameseField, gbc, 3);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel defLabel = new JLabel("Định nghĩa chi tiết:");
        defLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(defLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        JScrollPane defScroll = new JScrollPane(definitionArea);
        defScroll.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(defScroll, gbc);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel exLabel = new JLabel("Ví dụ:");
        exLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(exLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(exampleField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        
        // Initially hide all buttons except add and cancel
        updateButtonVisibility();

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3;
        formPanel.add(buttonPanel, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel.setForeground(UIUtils.TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addWord());
        updateButton.addActionListener(e -> updateWord());
        deleteButton.addActionListener(e -> deleteWord());
        cancelButton.addActionListener(e -> dispose());
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
        // Hide all buttons first
        addButton.setVisible(false);
        updateButton.setVisible(false);
        deleteButton.setVisible(false);
        cancelButton.setVisible(true); // Cancel button always visible
        
        // Show buttons based on current mode
        switch (currentMode) {
            case "add":
                addButton.setVisible(true);
                break;
            case "update":
                updateButton.setVisible(true);
                break;
            case "delete":
                deleteButton.setVisible(true);
                break;
        }
    }

    private void fillFields(Word word) {
        englishField.setText(word.getEnglishWord());
        partOfSpeechField.setSelectedItem(word.getPartOfSpeech());
        phoneticField.setText(word.getPhoneticSpelling());
        vietnameseField.setText(word.getVietnameseMeaning());
        definitionArea.setText(word.getDetailedDefinition());
        exampleField.setText(word.getExampleSentence());
    }

    private void setFieldsEditable(boolean editable) {
        englishField.setEditable(editable);
        partOfSpeechField.setEnabled(editable);
        phoneticField.setEditable(editable);
        vietnameseField.setEditable(editable);
        definitionArea.setEditable(editable);
        exampleField.setEditable(editable);
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
            JOptionPane.showMessageDialog(this, 
                "Thêm từ thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            parentGUI.refreshWordList();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Không thể thêm từ. Có thể từ này đã tồn tại.",
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateWord() {
        if (!validateFields()) return;

        Word word = createWordFromFields();
        boolean success = dictionaryDAO.updateWord(word);

        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Cập nhật từ thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
            parentGUI.refreshWordList();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Không thể cập nhật từ. Vui lòng thử lại.",
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, 
                    "Xóa từ thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
                parentGUI.refreshWordList();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Không thể xóa từ. Vui lòng thử lại.",
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
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
                "Các trường bắt buộc:\n" +
                "- Từ tiếng Anh\n" +
                "- Từ loại\n" +
                "- Phiên âm\n" +
                "- Nghĩa tiếng Việt\n" +
                "- Định nghĩa chi tiết",
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
        setFieldsEditable(true);
    }
}
