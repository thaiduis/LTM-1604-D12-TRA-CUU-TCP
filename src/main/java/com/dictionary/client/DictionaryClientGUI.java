package com.dictionary.client;

import com.dictionary.model.Word;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Giao diện GUI cho người dùng tra cứu từ điển Anh-Việt
 */
public class DictionaryClientGUI extends JFrame {
    private DictionaryClient client;
    private JTextField searchField;
    private JTextArea resultArea;
    private JTable wordTable;
    private DefaultTableModel tableModel;

    public DictionaryClientGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        connectToServer();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Từ điển Anh-Việt - Tra cứu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Khởi tạo các component
        searchField = new JTextField(30);
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setWrapStyleWord(true);
        resultArea.setLineWrap(true);

        // Bảng hiển thị kết quả tìm kiếm
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
        wordTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Từ tiếng Anh
        wordTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Từ loại
        wordTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Phiên âm
        wordTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Nghĩa tiếng Việt
        wordTable.getColumnModel().getColumn(4).setPreferredWidth(250); // Định nghĩa chi tiết
        wordTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Ví dụ
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel tìm kiếm ở trên cùng
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Nhập từ cần tra:"));
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JButton searchContainingButton = new JButton("Tìm từ chứa");
        searchContainingButton.addActionListener(e -> performSearchContaining());
        searchPanel.add(searchContainingButton);

        JButton refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> clearFields());
        searchPanel.add(refreshButton);

        // Panel kết quả tìm kiếm ở giữa
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Kết quả tra cứu chi tiết"));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Panel chứa bảng từ điển ở dưới
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách từ tìm thấy"));
        JScrollPane scrollPane = new JScrollPane(wordTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Layout tổng thể
        add(searchPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Xử lý phím Enter trong ô tìm kiếm
        searchField.addActionListener(e -> performSearch());

        // Xử lý khi chọn từ trong bảng
        wordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = wordTable.getSelectedRow();
                if (selectedRow >= 0) {
                    displayWordDetails(
                        (String) tableModel.getValueAt(selectedRow, 0),
                        (String) tableModel.getValueAt(selectedRow, 1),
                        (String) tableModel.getValueAt(selectedRow, 2),
                        (String) tableModel.getValueAt(selectedRow, 3),
                        (String) tableModel.getValueAt(selectedRow, 4),
                        (String) tableModel.getValueAt(selectedRow, 5)
                    );
                }
            }
        });
    }

    private void connectToServer() {
        try {
            client = new DictionaryClient();
            resultArea.setText("Đã kết nối đến server từ điển!\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Không thể kết nối đến server: " + e.getMessage(), 
                "Lỗi kết nối", 
                JOptionPane.ERROR_MESSAGE);
            resultArea.setText("Lỗi kết nối: " + e.getMessage() + "\n");
        }
    }

    private void performSearch() {
        String word = searchField.getText().trim();
        if (word.isEmpty()) {
            resultArea.setText("Vui lòng nhập từ cần tra cứu!");
            return;
        }

        if (client == null || !client.isConnected()) {
            resultArea.setText("Không có kết nối đến server!");
            return;
        }

        List<Word> results = client.searchWord(word);
        displaySearchResults(results, "Kết quả tra cứu cho từ '" + word + "':");
    }

    private void performSearchContaining() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            resultArea.setText("Vui lòng nhập từ khóa cần tìm!");
            return;
        }

        if (client == null || !client.isConnected()) {
            resultArea.setText("Không có kết nối đến server!");
            return;
        }

        List<Word> words = client.searchWordsContaining(keyword);
        displaySearchResults(words, "Các từ chứa '" + keyword + "':");
    }

    private void displaySearchResults(List<Word> words, String header) {
        if (words.isEmpty()) {
            resultArea.setText("Không tìm thấy từ nào.");
            tableModel.setRowCount(0);
        } else {
            resultArea.setText(header + "\n\nChọn một từ trong bảng bên dưới để xem chi tiết.");
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
    }

    private void displayWordDetails(String englishWord, String partOfSpeech, String phonetic,
                                  String meaning, String definition, String example) {
        StringBuilder details = new StringBuilder();
        details.append("Từ: ").append(englishWord).append("\n");
        details.append("Phiên âm: ").append(phonetic).append("\n");
        details.append("Từ loại: ").append(partOfSpeech).append("\n\n");
        details.append("Nghĩa: ").append(meaning).append("\n\n");
        details.append("Định nghĩa chi tiết:\n").append(definition).append("\n\n");
        if (example != null && !example.isEmpty()) {
            details.append("Ví dụ:\n").append(example);
        }
        resultArea.setText(details.toString());
    }

    private void clearFields() {
        searchField.setText("");
        resultArea.setText("");
        tableModel.setRowCount(0);
        wordTable.clearSelection();
    }

    @Override
    public void dispose() {
        if (client != null) {
            client.disconnect();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DictionaryClientGUI();
        });
    }
}