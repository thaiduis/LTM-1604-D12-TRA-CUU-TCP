package com.dictionary.server;

import com.dictionary.model.Word;
import com.dictionary.database.DictionaryDAO;
import com.dictionary.ui.UIUtils;
import com.dictionary.ui.UIUtils.AnimatedPanel;
import com.dictionary.ui.UIUtils.ModernTableCellRenderer;
import com.dictionary.ui.UIUtils.SlidePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DictionaryServerGUI extends JFrame {
    private DictionaryServer server;
    private DictionaryDAO dictionaryDAO;
    private JTable wordTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    
    private JTextField englishField;
    private JComboBox<String> partOfSpeechField;
    private JTextField phoneticField;
    private JTextField vietnameseField;
    private JTextArea definitionArea;
    private JTextField exampleField;
    private AnimatedPanel mainPanel;
    private SlidePanel formPanel;
    private JButton startStopButton;
    private JButton toggleFormButton;
    private JButton refreshButton;

    public DictionaryServerGUI() {
        this.dictionaryDAO = new DictionaryDAO();
        UIUtils.setUIStyle();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refreshWordList();
        startServer();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Từ điển Anh-Việt - Quản trị");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);

        // Status label
        statusLabel = new JLabel("Server đang dừng", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(UIUtils.ACCENT_COLOR);

        // Server control button
        startStopButton = UIUtils.createStyledButton("▶ Khởi động Server", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        toggleFormButton = UIUtils.createStyledButton("Hiện form", UIUtils.SECONDARY_COLOR, new Color(120, 50, 200));

        // Initialize table
        String[] columnNames = {"Từ tiếng Anh", "Từ loại", "Phiên âm", "Nghĩa tiếng Việt", "Định nghĩa chi tiết", "Ví dụ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordTable.setRowHeight(40);
        wordTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        wordTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        wordTable.setShowGrid(true);
        wordTable.setGridColor(new Color(200, 200, 200));  // Darker grid for better visibility
        wordTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        wordTable.getTableHeader().setBackground(new Color(240, 242, 245));  // Light background
        wordTable.getTableHeader().setForeground(new Color(0, 0, 0));  // Black text

        // Column widths
        TableColumnModel columnModel = wordTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(300);
        columnModel.getColumn(5).setPreferredWidth(250);

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

        // Refresh button (đưa lên phía trên danh sách)
        refreshButton = UIUtils.createStyledButton("Làm mới", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
        refreshButton.addActionListener(e -> reloadWordsAsync());

        mainPanel = new AnimatedPanel(new BorderLayout(20, 20));
        formPanel = new SlidePanel();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header with distinct gradient
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
                
                // Add subtle pattern
                g2.setColor(new Color(255, 255, 255, 60));
                for (int i = 0; i < getHeight(); i += 3) {
                    g2.drawLine(0, i, getWidth(), i);
                }
                g2.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout(15, 15));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        headerPanel.setOpaque(false);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setOpaque(false);
        controlPanel.add(statusLabel);
        controlPanel.add(startStopButton);
        headerPanel.add(controlPanel, BorderLayout.CENTER);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(250, 251, 255),
                    0, getHeight(), new Color(240, 242, 245)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        tablePanel.setOpaque(false);

        // Header cho bảng với tiêu đề và nút Làm mới bên phải
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Từ điển", SwingConstants.LEFT);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(UIUtils.TEXT_PRIMARY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(refreshButton);
        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(rightHeader, BorderLayout.EAST);

        JScrollPane tableScroll = new JScrollPane(wordTable);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
            UIUtils.createRoundBorder(UIUtils.DIVIDER_COLOR, 15),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        tableScroll.getViewport().setBackground(new Color(252, 253, 255));

        tablePanel.add(tableHeader, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Form panel setup
        JPanel formContainer = createFormPanel();
        formPanel.add(formContainer);
        formPanel.setToggleButton(toggleFormButton);

        // Layout assembly
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(toggleFormButton, BorderLayout.NORTH);
        bottomPanel.add(formPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        mainPanel.startAnimation();
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Create gradient background for form
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(240, 242, 255)  // Light blue tint
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        formContainer.setOpaque(false);
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form title
        JLabel formTitle = new JLabel("THÊM/SỬA TỪ", SwingConstants.CENTER);
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(UIUtils.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        formContent.add(formTitle, gbc);

        // Form fields
        addFormField(formContent, "Từ tiếng Anh:", englishField, gbc, 1);
        addFormField(formContent, "Từ loại:", partOfSpeechField, gbc, 2);
        addFormField(formContent, "Phiên âm:", phoneticField, gbc, 3);
        addFormField(formContent, "Nghĩa tiếng Việt:", vietnameseField, gbc, 4);

        gbc.gridx = 0; gbc.gridy = 5;
        JLabel defLabel = new JLabel("Định nghĩa chi tiết:");
        defLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formContent.add(defLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        JScrollPane defScroll = new JScrollPane(definitionArea);
        defScroll.setBorder(BorderFactory.createEmptyBorder());
        formContent.add(defScroll, gbc);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel exLabel = new JLabel("Ví dụ:");
        exLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formContent.add(exLabel, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        formContent.add(exampleField, gbc);

        // Action buttons with new colors
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton addButton = UIUtils.createStyledButton("Thêm từ mới", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        JButton updateButton = UIUtils.createStyledButton("Cập nhật", UIUtils.PRIMARY_COLOR, new Color(20, 70, 220));
        JButton deleteButton = UIUtils.createStyledButton("Xóa từ", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));
        JButton clearButton = UIUtils.createStyledButton("Xóa trường", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
        JButton refreshButton = UIUtils.createStyledButton("Làm mới", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        addButton.addActionListener(e -> addWord());
        updateButton.addActionListener(e -> updateWord());
        deleteButton.addActionListener(e -> deleteWord());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> refreshWordList());

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3;
        formContent.add(buttonPanel, gbc);

        formContainer.add(formContent, BorderLayout.CENTER);
        return formContainer;
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
        startStopButton.addActionListener(e -> toggleServer());

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
                    if (!formPanel.isShowing()) {
                        formPanel.toggle();
                    }
                }
            }
        });
    }

    private void toggleServer() {
        if (server == null || !server.isRunning()) {
            startServer();
        } else {
            stopServer();
        }
    }

    private void startServer() {
        try {
            server = new DictionaryServer(this);
            server.start();
            statusLabel.setText("Server đang chạy");
            statusLabel.setForeground(UIUtils.SUCCESS_COLOR);
            startStopButton.setText("Dừng Server");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Không thể khởi động server: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
            statusLabel.setText("Server đang dừng");
            statusLabel.setForeground(UIUtils.ACCENT_COLOR);
            startStopButton.setText("Khởi động Server");
        }
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
            refreshWordList();
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
            refreshWordList();
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
                clearFields();
                refreshWordList();
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

    private void reloadWordsAsync() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Đang tải dữ liệu...");
        new Thread(() -> {
            List<Word> words = dictionaryDAO.getAllWords();
            SwingUtilities.invokeLater(() -> {
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
                statusLabel.setText(server != null && server.isRunning() ? "Server đang chạy" : "Server đang dừng");
                refreshButton.setEnabled(true);
            });
        }).start();
    }

    @Override
    public void dispose() {
        stopServer();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DictionaryServerGUI();
        });
    }
}
