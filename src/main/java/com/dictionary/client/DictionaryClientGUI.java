package com.dictionary.client;

import com.dictionary.model.Word;
import com.dictionary.ui.UIUtils;
import com.dictionary.ui.UIUtils.AnimatedPanel;
import com.dictionary.ui.MaterialUIUtils;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DictionaryClientGUI extends JFrame {
    private DictionaryClient client;
    private JTextField searchField;
    private JTable wordTable;
    private DefaultTableModel tableModel;
    private AnimatedPanel mainPanel;
    private JLabel connectionStatusLabel;
    private JButton searchButton, searchContainingButton, refreshButton, reconnectButton;
    private Timer connectionTimer;
    private DefaultTableModel historyModel; 
    private JTable historyTable;
    private JPanel historyContentPanel;
    private boolean fromHistoryClick = false;
    private JComboBox<String> directionCombo; 
    private CSVLogger csvLogger; 
    private JPopupMenu suggestionPopup;
    

    // Panel chứa card chi tiết
    private JPanel detailContentPanel;

    public DictionaryClientGUI() {
        UIUtils.setUIStyle();
        csvLogger = CSVLogger.getInstance(); // Khởi tạo CSV logger
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        connectToServer();
        startConnectionMonitor();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Từ điển Anh-Việt - Dictionary Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MaterialUIUtils.BACKGROUND);

        suggestionPopup = new JPopupMenu();


        // Trạng thái kết nối
        connectionStatusLabel = new JLabel("Đang kết nối...", SwingConstants.CENTER);
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectionStatusLabel.setForeground(MaterialUIUtils.PRIMARY_COLOR);

        // Search field Material
        searchField = MaterialUIUtils.createSearchField();

        // Timer cho gợi ý và tự động tìm kiếm
        javax.swing.Timer suggestionTimer = new javax.swing.Timer(300, e -> showSuggestions());
        suggestionTimer.setRepeats(false);
        
        javax.swing.Timer searchTimer = new javax.swing.Timer(500, e -> performAutoSearch());
        searchTimer.setRepeats(false);
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                suggestionTimer.restart();
                searchTimer.restart(); // Tự động tìm kiếm
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                suggestionTimer.restart();
                searchTimer.restart(); // Tự động tìm kiếm
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                suggestionTimer.restart();
                searchTimer.restart(); // Tự động tìm kiếm
            }
        });
        
        
        // Direction combo (Anh -> Việt, Việt -> Anh)
        directionCombo = MaterialUIUtils.createMaterialComboBox(
    new String[]{"Anh → Việt", "Việt → Anh"},
    new Color(156, 39, 176), // màu tím
    10                 // radius bo góc
);





        // Bảng kết quả
        String[] columnNames = {"Từ tiếng Anh", "Từ loại", "Phiên âm", "Nghĩa tiếng Việt", "Định nghĩa", "Ví dụ", "Ảnh"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Table setup
        // Table setup
// Bảng kết quả với JTextArea renderer để xuống dòng
        wordTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Object value = getValueAt(row, column);
                JTextArea textArea = new JTextArea(value == null ? "" : value.toString());
                textArea.setLineWrap(true);             // bật xuống dòng
                textArea.setWrapStyleWord(true);        // xuống dòng theo từ
                textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                textArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                textArea.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));

                // Nếu chọn thì đổi màu nền
                if (isRowSelected(row)) {
                    textArea.setBackground(new Color(227, 242, 253));
                }

                // Tự động điều chỉnh chiều cao theo nội dung
                int preferredHeight = textArea.getPreferredSize().height + 20;
                if (getRowHeight(row) != preferredHeight) {
                    setRowHeight(row, preferredHeight);
                }

                return textArea;
            }
        };

        // Header style
        wordTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        wordTable.getTableHeader().setBackground(new Color(33, 150, 243));
        wordTable.getTableHeader().setForeground(Color.BLACK);
        wordTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        wordTable.getTableHeader().setReorderingAllowed(false);

        // Tự động giãn cột
        wordTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


        // Tăng chiều cao row
        wordTable.setRowHeight(50);


        // Độ rộng cột
        TableColumnModel columnModel = wordTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(250);
        columnModel.getColumn(5).setPreferredWidth(200);
        columnModel.getColumn(6).setPreferredWidth(80); // Cột ảnh

        // Các nút
        searchButton = MaterialUIUtils.createMaterialButton("Tìm kiếm", MaterialUIUtils.PRIMARY_COLOR);
        searchContainingButton = MaterialUIUtils.createMaterialButton("Tìm từ chứa", MaterialUIUtils.ACCENT_COLOR);
        refreshButton = MaterialUIUtils.createMaterialButton("Làm mới", new Color(244, 67, 54));
        reconnectButton = MaterialUIUtils.createMaterialButton("Kết nối lại", MaterialUIUtils.PRIMARY_DARK);
        reconnectButton.setVisible(false);

        // Panel chính
        mainPanel = new AnimatedPanel(new BorderLayout(20, 20));
    }

    private void showSuggestions() {
        String text = searchField.getText().trim();
        suggestionPopup.setVisible(false);
        suggestionPopup.removeAll();
    
        if (text.isEmpty() || client == null || !client.isConnected()) return;
    
        String direction = (String) directionCombo.getSelectedItem();
        List<Word> suggestions;
    
        if ("Anh → Việt".equals(direction)) {
            suggestions = client.searchWordsContaining(text);
        } else {
            suggestions = client.searchVietnameseWordsContaining(text);
        }
    
        if (suggestions.isEmpty()) return;
    
        // ✅ Chỉ lấy 5 gợi ý đầu tiên
        int limit = Math.min(5, suggestions.size());
    
        for (int i = 0; i < limit; i++) {
            Word w = suggestions.get(i);
    
            String label = "Anh → Việt".equals(direction)
                    ? (w.getEnglishWord() + " - " + w.getVietnameseMeaning())
                    : (w.getVietnameseMeaning() + " - " + w.getEnglishWord());
    
            JMenuItem item = new JMenuItem(label);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.addActionListener(e -> {
                if ("Anh → Việt".equals(direction)) {
                    searchField.setText(w.getEnglishWord());
                } else {
                    searchField.setText(w.getVietnameseMeaning());
                }
                suggestionPopup.setVisible(false);
                performSearch();
            });
            suggestionPopup.add(item);
        }
    
        try {
            Rectangle rect = searchField.modelToView(searchField.getDocument().getLength());
            suggestionPopup.show(searchField, rect.x, rect.y + 30);
        } catch (Exception ignored) {}
    }
    
    

    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
    
        // Header
        JPanel headerPanel = MaterialUIUtils.createCardPanel(new BorderLayout(10, 10));
        headerPanel.add(connectionStatusLabel, BorderLayout.NORTH);
    
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField);
        searchPanel.add(directionCombo);
        searchPanel.add(searchButton);
        searchPanel.add(searchContainingButton);
        searchPanel.add(refreshButton);
        searchPanel.add(reconnectButton);

        headerPanel.add(searchPanel, BorderLayout.CENTER);

        // Chi tiết từ (card container)
        JPanel detailPanel = MaterialUIUtils.createCardPanel(new BorderLayout(10, 10));
        JLabel detailTitle = new JLabel("Chi tiết từ");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        detailPanel.add(detailTitle, BorderLayout.NORTH);
    
        detailContentPanel = new JPanel();
        detailContentPanel.setLayout(new BoxLayout(detailContentPanel, BoxLayout.Y_AXIS));
        detailContentPanel.setBackground(MaterialUIUtils.BACKGROUND);
    
        JScrollPane detailScroll = new JScrollPane(detailContentPanel);
        detailScroll.setBorder(null);
        detailPanel.add(detailScroll, BorderLayout.CENTER);
    
        // Bảng kết quả tìm kiếm
        JPanel tablePanel = MaterialUIUtils.createCardPanel(new BorderLayout(10, 10));
        JLabel tableTitle = new JLabel("Kết quả tìm kiếm");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(wordTable), BorderLayout.CENTER);
    
        // Bảng lịch sử tra cứu
        String[] historyColumns = {"Từ đã tra"};
        historyModel = new DefaultTableModel(historyColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(28);
    
        // Panel lịch sử tra cứu
        JPanel historyPanel = MaterialUIUtils.createCardPanel(new BorderLayout(10, 10));
        JLabel historyTitle = new JLabel("Lịch sử tra cứu");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        historyPanel.add(historyTitle, BorderLayout.NORTH);

        // Container cho lịch sử
        historyContentPanel = new JPanel();
        historyContentPanel.setLayout(new BoxLayout(historyContentPanel, BoxLayout.Y_AXIS));
        historyContentPanel.setBackground(Color.WHITE);

        JScrollPane historyScroll = new JScrollPane(historyContentPanel);
        historyScroll.setBorder(null);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        // SplitPane dọc cho bên phải (kết quả + lịch sử)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, historyPanel);
        rightSplit.setResizeWeight(0.5); 
        rightSplit.setOneTouchExpandable(true);

        // SplitPane ngang chính (chi tiết từ | bên phải)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailPanel, rightSplit);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);

        // Sau khi giao diện render xong thì đặt vị trí divider 50:50
        SwingUtilities.invokeLater(() -> {
            rightSplit.setDividerLocation(0.5); 
            splitPane.setDividerLocation(0.3);
        });

    
        // Thêm vào main
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
    
        add(mainPanel);
        mainPanel.startAnimation();
    }
    // helper: escape nếu cần (đề phòng có ký tự đặc biệt)

private static String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
}

private void addHistoryItem(String directionLabel, Word w) {
    JPanel item = new JPanel(new BorderLayout(10,5)) {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200,230,201));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),15,15);
            g2.dispose();
        }
    };
    item.setOpaque(false);
    item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

    // 👉 Lưu Word & hướng dịch trên component để dùng lại khi click
    item.putClientProperty("word", w);
    item.putClientProperty("dir", directionLabel);

    String time = java.time.LocalTime.now().withNano(0).toString();
    boolean viToEn = "Việt → Anh".equals(directionLabel);

    String left  = viToEn ? esc(w.getVietnameseMeaning()) : esc(w.getEnglishWord());
    String right = viToEn ? esc(w.getEnglishWord())       : esc(w.getVietnameseMeaning());

    JLabel text = new JLabel("<html><b>"+esc(directionLabel)+":</b> "
            + left + " → " + right
            + " <font color='gray'>(" + time + ")</font></html>");
    text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    text.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));

    // Nút xoá
    JButton deleteBtn = new JButton() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? new Color(229,57,53) : new Color(244,67,54));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            String s = "X";
            g2.drawString(s, (getWidth()-fm.stringWidth(s))/2, (getHeight()+fm.getAscent())/2-3);
            g2.dispose();
        }
    };
    deleteBtn.setPreferredSize(new Dimension(30,30));
    deleteBtn.setFocusPainted(false);
    deleteBtn.setBorderPainted(false);
    deleteBtn.setContentAreaFilled(false);
    deleteBtn.addActionListener(e -> {
        int index = -1;
        for (int i = 0; i < historyContentPanel.getComponentCount(); i++) {
            if (historyContentPanel.getComponent(i) == item) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            historyContentPanel.remove(index);
            if (index < historyContentPanel.getComponentCount()
                && historyContentPanel.getComponent(index) instanceof Box.Filler) {
                historyContentPanel.remove(index);
            }
        }
        historyContentPanel.revalidate();
        historyContentPanel.repaint();
    });

    // 👉 Click vào item lịch sử => hiện lại chi tiết + dán vào ô tìm kiếm
    item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    item.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override public void mouseClicked(java.awt.event.MouseEvent e) {
            Word ww = (Word) item.getClientProperty("word");
            String dir = (String) item.getClientProperty("dir");

            // dán từ lại vào searchField
            searchField.setText("Anh → Việt".equals(dir)
                                ? ww.getEnglishWord()
                                : ww.getVietnameseMeaning());

            // hiển thị chi tiết
            displayWordDetails(
    ww.getEnglishWord(),
    ww.getPartOfSpeech(),
    ww.getPhoneticSpelling(),
    ww.getVietnameseMeaning(),
    ww.getDetailedDefinition(),
    ww.getExampleSentence(),
    ww.getImagePath() // ✅ thêm ảnh
);

        }
    });

    item.add(text, BorderLayout.CENTER);
    item.add(deleteBtn, BorderLayout.EAST);

    historyContentPanel.add(item, 0);
    historyContentPanel.add(Box.createVerticalStrut(6), 1);
    historyContentPanel.revalidate();
    historyContentPanel.repaint();
}

    
    

    private void setupEventHandlers() {
        searchButton.addActionListener(e -> performSearch());
        searchContainingButton.addActionListener(e -> performSearchContaining());
        refreshButton.addActionListener(e -> clearFields());
        reconnectButton.addActionListener(e -> connectToServer());

        wordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = wordTable.getSelectedRow();
                if (row >= 0) {
                    // Lấy lại Word object từ server để có đầy đủ thông tin ảnh
                    String englishWord = (String) tableModel.getValueAt(row, 0);
                    List<Word> fullWords = client.searchWord(englishWord);
                    
                    Word w = null;
                    if (!fullWords.isEmpty()) {
                        // Tìm từ có cùng từ loại
                        String partOfSpeech = (String) tableModel.getValueAt(row, 1);
                        for (Word word : fullWords) {
                            if (word.getPartOfSpeech().equals(partOfSpeech)) {
                                w = word;
                                break;
                            }
                        }
                        if (w == null) w = fullWords.get(0); // Fallback
                    } else {
                        // Tạo Word object từ dữ liệu bảng nếu không tìm thấy
                        w = new Word(
                            (String) tableModel.getValueAt(row, 0), // English
                            (String) tableModel.getValueAt(row, 1), // POS
                            (String) tableModel.getValueAt(row, 2), // Phonetic
                            (String) tableModel.getValueAt(row, 3), // Vietnamese
                            (String) tableModel.getValueAt(row, 4), // Definition
                            (String) tableModel.getValueAt(row, 5), // Example
                            null  // ImagePath
                        );
                    }
                    displayWordDetails(
    w.getEnglishWord(),
    w.getPartOfSpeech(),
    w.getPhoneticSpelling(),
    w.getVietnameseMeaning(),
    w.getDetailedDefinition(),
    w.getExampleSentence(),
    w.getImagePath() // ✅
);


                    if (!fromHistoryClick) {
                        String dir = (String) directionCombo.getSelectedItem();
                        addHistoryItem(dir, w);   // 👉 chỉ lưu khi user chọn chi tiết
                        
                        // Ghi log CSV khi user chọn từ trong bảng
                        csvLogger.logSearch(w.getEnglishWord(), dir, w);
                    }
                    fromHistoryClick = false;
                }
            }
        });
        
    }

    private void connectToServer() {
        try {
            if (client != null) {
                client.disconnect();
            }
            client = new DictionaryClient();
            updateConnectionUI(true);
            showMessageCard("Đã kết nối đến server thành công!\nNhập từ để bắt đầu tra cứu.");
            JOptionPane.showMessageDialog(this, 
                "Kết nối thành công đến server!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            updateConnectionUI(false);
            showMessageCard("✗ Không thể kết nối đến server.\nVui lòng kiểm tra:\n• Server đã được khởi động chưa?\n• Cổng 12345 có bị chặn không?");
            JOptionPane.showMessageDialog(this, 
                "Không thể kết nối đến server!\n\n" +
                "Lỗi: " + e.getMessage() + "\n\n" +
                "Vui lòng:\n" +
                "1. Kiểm tra server đã được khởi động\n" +
                "2. Đảm bảo cổng 12345 không bị chặn\n" +
                "3. Nhấn nút 'Kết nối lại' để thử lại", 
                "Lỗi kết nối", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateConnectionUI(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("ĐÃ KẾT NỐI SERVER");
            connectionStatusLabel.setForeground(UIUtils.SUCCESS_COLOR);
        } else {
            connectionStatusLabel.setText("MẤT KẾT NỐI");
            connectionStatusLabel.setForeground(UIUtils.ACCENT_COLOR);
        }
        searchButton.setEnabled(connected);
        searchContainingButton.setEnabled(connected);
        refreshButton.setEnabled(true);
        reconnectButton.setVisible(!connected);
    }

    private void startConnectionMonitor() {
        connectionTimer = new Timer(2000, e -> {
            boolean connected = client != null && client.isConnected();
            updateConnectionUI(connected);
        });
        connectionTimer.setInitialDelay(0);
        connectionTimer.start();
    }

    private void performSearch() {
        String word = searchField.getText().trim();
        performSearchWithText(word, true); // Log activity
    }
    
    private void performAutoSearch() {
        String word = searchField.getText().trim();
        performSearchWithText(word, false); // Không log cho auto search
    }
    
    private void performSearchWithText(String word, boolean logActivity) {
        if (word.isEmpty()) {
            showMessageCard("Nhập từ để bắt đầu tra cứu.");
            tableModel.setRowCount(0);
            return;
        }

        if (client == null || !client.isConnected()) {
            showMessageCard("Không có kết nối đến server!");
            return;
        }

        String direction = (String) directionCombo.getSelectedItem();
        List<Word> results;
        String header;
        
        // Tìm kiếm chính xác trước, nếu không có thì tìm chứa
        if ("Anh → Việt".equals(direction)) {
            results = client.searchWord(word);
            if (results.isEmpty()) {
                results = client.searchWordsContaining(word);
                header = "Các từ chứa \"" + word + "\"";
            } else {
                header = "Kết quả cho từ \"" + word + "\"";
            }
        } else {
            results = client.searchVietnameseWord(word);
            if (results.isEmpty()) {
                results = client.searchVietnameseWordsContaining(word);
                header = "Các từ Việt chứa \"" + word + "\"";
            } else {
                header = "Kết quả cho từ Việt \"" + word + "\"";
            }
        }
        
        displaySearchResults(results, header);
    
        // Chỉ log khi user chủ động tìm kiếm và có kết quả
        if (logActivity && !fromHistoryClick && results.size() == 1) {
            String dir = (String) directionCombo.getSelectedItem();
            addHistoryItem(dir, results.get(0));
            csvLogger.logSearch(word, dir, results.get(0));
        }
        fromHistoryClick = false;
        SwingUtilities.invokeLater(() -> searchField.requestFocusInWindow());
    }
    

    private void performSearchContaining() {
        String key = searchField.getText().trim();
        if (key.isEmpty()) {
            showMessageCard("Vui lòng nhập từ khóa.");
            return;
        }

        String direction = (String) directionCombo.getSelectedItem();
        List<Word> results;
        String header;
    
        if ("Anh → Việt".equals(direction)) {
            results = client.searchWordsContaining(key);
            header = "Các từ chứa \"" + key + "\"";
        } else {
            results = client.searchVietnameseWordsContaining(key);
            header = "Các từ Việt chứa \"" + key + "\"";
        }
    
        displaySearchResults(results, header);
    
        // ✅ Nếu chỉ có 1 kết quả thì show chi tiết và ghi log CSV
        if (results.size() == 1) {
            Word w = results.get(0);
            displayWordDetails(
                    w.getEnglishWord(), w.getPartOfSpeech(), w.getPhoneticSpelling(),
                    w.getVietnameseMeaning(), w.getDetailedDefinition(), w.getExampleSentence(), w.getImagePath()
            );
            
            // Ghi log CSV cho tìm kiếm chứa
            csvLogger.logSearch(key, direction, w);
        }
    }
    
    

    private void displaySearchResults(List<Word> words, String header) {
    tableModel.setRowCount(0);
        if (words.isEmpty()) {
        showMessageCard("Không tìm thấy kết quả.");
        return;
    }
    for (Word w : words) {
        // Tạo icon ảnh nhỏ cho bảng
        String imageDisplay = "Không có";
        if (w.getImagePath() != null && !w.getImagePath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(w.getImagePath());
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                imageDisplay = "Có ảnh";
            } catch (Exception e) {
                imageDisplay = "Lỗi ảnh";
            }
        }
        
        tableModel.addRow(new Object[]{
                w.getEnglishWord(), w.getPartOfSpeech(), w.getPhoneticSpelling(),
                w.getVietnameseMeaning(), w.getDetailedDefinition(), w.getExampleSentence(),
                imageDisplay
        });
    }
    showMessageCard(header + " (" + words.size() + " kết quả)");
}

private void displayWordDetails(String eng, String pos, String phon, 
                                String vn, String def, String ex, String imgPath) {
    detailContentPanel.removeAll();

    // Từ vựng
    detailContentPanel.add(createDetailCard("Từ vựng", eng + "  /" + (phon==null?"":phon) + "/  (" + (pos==null?"":pos) + ")"));
    detailContentPanel.add(Box.createVerticalStrut(12));

    // Nghĩa
    detailContentPanel.add(createDetailCard("Nghĩa tiếng Việt", vn==null?"":vn));
    detailContentPanel.add(Box.createVerticalStrut(12));

    // Định nghĩa
    detailContentPanel.add(createDetailCard("Định nghĩa", def==null?"":def));
    detailContentPanel.add(Box.createVerticalStrut(12));

    // Ví dụ
    if (ex != null && !ex.isEmpty()) {
        detailContentPanel.add(createDetailCard("Ví dụ", ex));
        detailContentPanel.add(Box.createVerticalStrut(12));
    }

    // ✅ Ảnh minh họa
    if (imgPath != null && !imgPath.isEmpty()) {
        try {
            java.io.File imageFile = new java.io.File(imgPath);
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imgPath);
                
                // Kiểm tra xem có phải GIF không
                boolean isGif = imgPath.toLowerCase().endsWith(".gif");
                
                // Tính toán kích thước ảnh phù hợp
                int originalWidth = icon.getIconWidth();
                int originalHeight = icon.getIconHeight();
                int maxSize = 250;
                
                JLabel imgLabel;
                
                if (isGif) {
                    // Với GIF, giữ nguyên ImageIcon để animation hoạt động
                    // Nhưng vẫn scale nếu quá lớn
                    if (originalWidth > maxSize || originalHeight > maxSize) {
                        int newWidth, newHeight;
                        if (originalWidth > originalHeight) {
                            newWidth = maxSize;
                            newHeight = (originalHeight * maxSize) / originalWidth;
                        } else {
                            newHeight = maxSize;
                            newWidth = (originalWidth * maxSize) / originalHeight;
                        }
                        // Scale GIF nhưng giữ animation
                        Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
                        imgLabel = new JLabel(new ImageIcon(scaledImage));
                    } else {
                        // GIF nhỏ, hiển thị nguyên bản
                        imgLabel = new JLabel(icon);
                    }
                } else {
                    // Với ảnh tĩnh, dùng SCALE_SMOOTH cho chất lượng tốt
                    int newWidth, newHeight;
                    if (originalWidth > originalHeight) {
                        newWidth = maxSize;
                        newHeight = (originalHeight * maxSize) / originalWidth;
                    } else {
                        newHeight = maxSize;
                        newWidth = (originalWidth * maxSize) / originalHeight;
                    }
                    Image img = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    imgLabel = new JLabel(new ImageIcon(img));
                }
                
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                // Tạo panel chứa ảnh với viền đẹp
                JPanel imagePanel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.setColor(new Color(0, 0, 0, 30));
                        g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 12, 12);
                        g2.dispose();
                    }
                };
                imagePanel.setOpaque(false);
                imagePanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
                
                JLabel titleLabel = new JLabel("Ảnh minh họa");
                titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
                titleLabel.setForeground(new Color(26, 115, 232));
                titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                imagePanel.add(titleLabel, BorderLayout.NORTH);
                imagePanel.add(imgLabel, BorderLayout.CENTER);
                
                detailContentPanel.add(imagePanel);
                detailContentPanel.add(Box.createVerticalStrut(12));
            } else {
                detailContentPanel.add(createDetailCard("Ảnh minh họa", "File ảnh không tồn tại: " + imgPath));
                detailContentPanel.add(Box.createVerticalStrut(12));
            }
        } catch (Exception e) {
            detailContentPanel.add(createDetailCard("Ảnh minh họa", "Không thể tải ảnh: " + e.getMessage()));
            detailContentPanel.add(Box.createVerticalStrut(12));
        }
    }

    detailContentPanel.revalidate();
    detailContentPanel.repaint();
}




    private JPanel createDetailCard(String title, String content) {
        JPanel card = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // nền trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // shadow mềm
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 12, 12);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Tiêu đề
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        titleLabel.setForeground(new Color(26, 115, 232));

        // Nội dung
        // Nội dung
JTextArea contentLabel = new JTextArea(content == null ? "" : content);
contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
contentLabel.setForeground(new Color(51, 51, 51));
contentLabel.setLineWrap(true);               // bật tự động xuống dòng
contentLabel.setWrapStyleWord(true);          // xuống dòng theo từ
contentLabel.setEditable(false);
contentLabel.setOpaque(false);
contentLabel.setBorder(null);


        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentLabel, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        return card;
    }

    private void showMessageCard(String msg) {
        detailContentPanel.removeAll();
        detailContentPanel.add(createDetailCard("Thông báo", msg));
        detailContentPanel.revalidate();
        detailContentPanel.repaint();
    }

    private void clearFields() {
        searchField.setText("");
        showMessageCard("Nhập từ để bắt đầu tra cứu.");
        tableModel.setRowCount(0);
        wordTable.clearSelection();
    }

    @Override
    public void dispose() {
        if (client != null) client.disconnect();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DictionaryClientGUI::new);
    }
}
