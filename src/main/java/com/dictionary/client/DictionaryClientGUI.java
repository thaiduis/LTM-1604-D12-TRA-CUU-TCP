package com.dictionary.client;

import com.dictionary.model.Word;
import com.dictionary.ui.UIUtils;
import com.dictionary.ui.UIUtils.AnimatedPanel;
import com.dictionary.ui.UIUtils.ModernTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DictionaryClientGUI extends JFrame {
    private DictionaryClient client;
    private JTextField searchField;
    private JEditorPane resultArea;
    private JTable wordTable;
    private DefaultTableModel tableModel;
    private AnimatedPanel mainPanel;
    private JLabel connectionStatusLabel;
    private JPanel searchPanel;
    private JPanel detailPanel;
    private JPanel tablePanel;
    private JButton searchButton;
    private JButton searchContainingButton;
    private JButton refreshButton;
    private JButton reconnectButton;
    private Timer connectionTimer;

    public DictionaryClientGUI() {
        UIUtils.setUIStyle();
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
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);

        // Connection status
        connectionStatusLabel = new JLabel("Đang kết nối...", SwingConstants.CENTER);
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectionStatusLabel.setForeground(UIUtils.TEXT_SECONDARY);
        connectionStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Search field
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(UIUtils.createRoundBorder(UIUtils.PRIMARY_COLOR, 15));
        searchField.setBackground(UIUtils.SURFACE_COLOR);
        searchField.setForeground(UIUtils.TEXT_PRIMARY);
        searchField.setHorizontalAlignment(JTextField.CENTER);
        
        // Result area (HTML for rich layout)
        resultArea = new JEditorPane();
        resultArea.setContentType("text/html; charset=UTF-8");
        resultArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        resultArea.setBackground(Color.WHITE);

        // Table setup
        String[] columnNames = {"Từ tiếng Anh", "Từ loại", "Phiên âm", "Nghĩa tiếng Việt", "Định nghĩa chi tiết", "Ví dụ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordTable.setRowHeight(45);
        wordTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        wordTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        wordTable.setShowGrid(true);
        wordTable.setGridColor(UIUtils.DIVIDER_COLOR);
        wordTable.setBackground(UIUtils.SURFACE_COLOR);
        wordTable.setForeground(UIUtils.TEXT_PRIMARY);
        wordTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        wordTable.getTableHeader().setBackground(new Color(240, 242, 245));  // Light background
        wordTable.getTableHeader().setForeground(new Color(0, 0, 0));  // Black text
        wordTable.getTableHeader().setReorderingAllowed(false);
        // Allow horizontal scroll instead of squashing columns when resizing
        wordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        TableColumnModel columnModel = wordTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(300);
        columnModel.getColumn(5).setPreferredWidth(250);

        mainPanel = new AnimatedPanel(new BorderLayout(20, 20));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel with distinct gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250, 250, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Add subtle pattern
                g2.setColor(new Color(255, 255, 255, 50));
                for (int i = 0; i < getHeight(); i += 3) {
                    g2.drawLine(0, i, getWidth(), i);
                }
                g2.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout(20, 20));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        headerPanel.setOpaque(false);

        // Search panel với layout cải thiện
        searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        searchPanel.setOpaque(false);

        // Search field panel
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setOpaque(false);
        searchFieldPanel.setBorder(UIUtils.createRoundBorder(UIUtils.PRIMARY_COLOR, 15));
        searchFieldPanel.add(searchField);
        
        searchField.setToolTipText("Nhập từ tiếng Anh cần tra cứu...");
        // Buttons
        searchButton = UIUtils.createStyledButton("Tìm kiếm", UIUtils.PRIMARY_COLOR, UIUtils.SECONDARY_COLOR);
        searchContainingButton = UIUtils.createStyledButton("Tìm từ chứa", UIUtils.SUCCESS_COLOR, UIUtils.SUCCESS_COLOR);
        refreshButton = UIUtils.createStyledButton("Làm mới", UIUtils.TEXT_SECONDARY, UIUtils.TEXT_SECONDARY);
        reconnectButton = UIUtils.createStyledButton("Kết nối lại", UIUtils.PRIMARY_COLOR, UIUtils.SECONDARY_COLOR);

        searchPanel.add(searchFieldPanel);
        searchPanel.add(searchButton);
        searchPanel.add(searchContainingButton);
        searchPanel.add(refreshButton);
        searchPanel.add(reconnectButton);

        // Add status and search to header
        JPanel topStatusPanel = new JPanel();
        topStatusPanel.setOpaque(false);
        topStatusPanel.setLayout(new BoxLayout(topStatusPanel, BoxLayout.Y_AXIS));
        connectionStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reconnectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reconnectButton.setVisible(false);
        topStatusPanel.add(connectionStatusLabel);
        topStatusPanel.add(Box.createVerticalStrut(8));
        topStatusPanel.add(reconnectButton);
        headerPanel.add(topStatusPanel, BorderLayout.NORTH);
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        // Detail panel (left)
        detailPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(252, 253, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        detailPanel.setOpaque(false);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 223, 228), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        detailPanel.setMinimumSize(new Dimension(420, 200));

        JLabel detailTitle = new JLabel("CHI TIẾT TỪ");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        detailTitle.setForeground(new Color(51, 51, 51));
        detailTitle.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
        
        detailPanel.add(detailTitle, BorderLayout.NORTH);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(null);
        resultScroll.setOpaque(true);
        resultScroll.getViewport().setOpaque(true);
        resultScroll.getViewport().setBackground(Color.WHITE);
        resultScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Add a gradient border
        JPanel resultWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 223, 228));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
                
                g2.dispose();
            }
        };
        resultWrapper.setOpaque(false);
        resultWrapper.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        resultWrapper.add(resultScroll);
        
        detailPanel.add(resultWrapper, BorderLayout.CENTER);

        // Table panel
        tablePanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(252, 253, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 223, 228), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        tablePanel.setMinimumSize(new Dimension(200, 200));

        JLabel tableTitle = new JLabel("KẾT QUẢ TÌM KIẾM");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(51, 51, 51));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));

        JScrollPane tableScroll = new JScrollPane(wordTable);
        tableScroll.setBorder(UIUtils.createRoundBorder(UIUtils.DIVIDER_COLOR, 10));
        tableScroll.getViewport().setBackground(UIUtils.SURFACE_COLOR);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Layout assembly: header on top, center split left-right
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailPanel, tablePanel);
        horizontalSplit.setResizeWeight(0.42);
        horizontalSplit.setContinuousLayout(true);
        horizontalSplit.setOneTouchExpandable(true);
        horizontalSplit.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(horizontalSplit, BorderLayout.CENTER);

        add(mainPanel);
        mainPanel.startAnimation();

        // Setup button actions
        searchButton.addActionListener(e -> performSearch());
        searchContainingButton.addActionListener(e -> performSearchContaining());
        refreshButton.addActionListener(e -> clearFields());
        reconnectButton.addActionListener(e -> {
            connectToServer();
        });
    }

    private void setupEventHandlers() {
        searchField.addActionListener(e -> performSearch());

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
            updateConnectionUI(true);
            String introHtml = "<h1 style='margin:0 0 6px 0;color:#333'>TỪ ĐIỂN ANH-VIỆT</h1>" +
                               "<hr style='border:none;height:1px;background:#e5e7eb;margin:6px 0 12px'/>" +
                               "<div style='color:#444;margin-bottom:6px'>Hướng dẫn sử dụng:</div>" +
                               "<ul style='margin:0 0 0 18px;color:#555'>" +
                               "<li>Nhập từ tiếng Anh vào ô tìm kiếm</li>" +
                               "<li>Nhấn 'Tìm kiếm' để tra cứu chính xác</li>" +
                               "<li>Nhấn 'Tìm từ chứa' để tìm các từ có chứa từ khóa</li>" +
                               "<li>Chọn từ trong bảng để xem chi tiết</li>" +
                               "<li>Nhấn 'Làm mới' để xóa kết quả</li>" +
                               "</ul>";
            resultArea.setText(wrapHtml(introHtml));
        } catch (IOException e) {
            updateConnectionUI(false);
            StringBuilder error = new StringBuilder();
            error.append("LỖI KẾT NỐI\n");
            error.append("----------------------------------------\n\n");
            error.append(e.getMessage()).append("\n\n");
            error.append("Hãy kiểm tra:\n\n");
            error.append("• Server có đang chạy không\n");
            error.append("• Kết nối mạng có ổn định không\n");
            error.append("• Port 12345 có bị chặn không");
            resultArea.setText(toParagraphHtml(error.toString()));
            JOptionPane.showMessageDialog(this, 
                "Không thể kết nối đến server: " + e.getMessage(),
                "Lỗi kết nối",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateConnectionUI(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("ĐÃ KẾT NỐI ĐẾN SERVER");
            connectionStatusLabel.setForeground(UIUtils.SUCCESS_COLOR);
        } else {
            connectionStatusLabel.setText("MẤT KẾT NỐI VỚI SERVER");
            connectionStatusLabel.setForeground(UIUtils.ACCENT_COLOR);
        }
        searchButton.setEnabled(connected);
        searchContainingButton.setEnabled(connected);
        refreshButton.setEnabled(true);
        reconnectButton.setVisible(!connected);

    }

    private void startConnectionMonitor() {
        connectionTimer = new Timer(1500, e -> {
            boolean connected = client != null && client.isConnected();
            updateConnectionUI(connected);
        });
        connectionTimer.setInitialDelay(0);
        connectionTimer.start();
    }

    private void performSearch() {
        String word = searchField.getText().trim();
        if (word.isEmpty()) {
            resultArea.setText(toParagraphHtml("Vui lòng nhập từ cần tra cứu!"));
            return;
        }

        if (client == null || !client.isConnected()) {
            resultArea.setText(toParagraphHtml("Không có kết nối đến server!"));
            return;
        }

        // Hiển thị trạng thái đang tìm kiếm
        resultArea.setText(toParagraphHtml("Đang tìm kiếm từ '" + word + "'..."));
        
        List<Word> results = client.searchWord(word);
        displaySearchResults(results, "<h1> Kết quả tra cứu cho từ '" + word + "':</h1>");
    }

    private void performSearchContaining() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            resultArea.setText(toParagraphHtml("Vui lòng nhập từ khóa cần tìm!"));
            return;
        }

        if (client == null || !client.isConnected()) {
            resultArea.setText(toParagraphHtml("Không có kết nối đến server!"));
            return;
        }

        // Hiển thị trạng thái đang tìm kiếm
        resultArea.setText(toParagraphHtml("Đang tìm các từ chứa '" + keyword + "'..."));
        
        List<Word> words = client.searchWordsContaining(keyword);
        displaySearchResults(words, "Các từ chứa '" + keyword + "':");
    }

    private void displaySearchResults(List<Word> words, String header) {
        StringBuilder message = new StringBuilder();
        if (words.isEmpty()) {
            message.append("<h2 style='margin:0 0 6px 0;color:#333'>Không tìm thấy kết quả</h2>");
            message.append("<div style='height:1px;background:#e5e7eb;margin:6px 0 10px'></div>");
            message.append("<div style='color:#555'>Gợi ý:<ul style='margin:6px 0 0 18px'>" +
                           "<li>Kiểm tra chính tả</li>" +
                           "<li>Thử tìm từ chứa từ khóa</li>" +
                           "<li>Sử dụng từ khóa ngắn gọn hơn</li>" +
                           "</ul></div>");
            resultArea.setText(wrapHtml(message.toString()));
            tableModel.setRowCount(0);
        } else {
        message.append("<div style='display:flex;align-items:baseline;gap:10px'>");
            message.append("<h2 style='margin:0;color:#333'>" + header + "</h2>");
        message.append("<span style='color:#666'>(" + words.size() + " kết quả)</span>");
            message.append("</div>");
        message.append("<div style='height:1px;background:#e5e7eb;margin:8px 0 10px'></div>");
            message.append("<div style='color:#555'>Chọn một từ trong bảng bên cạnh để xem chi tiết.</div>");
            resultArea.setText(wrapHtml(message.toString()));
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
        String html = buildWordDetailHtml(englishWord, partOfSpeech, phonetic, meaning, definition, example);
        resultArea.setText(html);
    }

    private String buildWordDetailHtml(String englishWord, String partOfSpeech, String phonetic,
                                       String meaning, String definition, String example) {
        String safeExample = example == null ? "" : example;
        String safePhon = phonetic == null ? "" : phonetic;
        String safeDef = definition == null ? "" : definition;
        String safeMeaning = meaning == null ? "" : meaning;

        StringBuilder sb = new StringBuilder();
        
        // Main container with modern design
        sb.append("<div class='word-details' style='max-width:800px;margin:0 auto;font-family:\"Segoe UI\",sans-serif'>");
        
        // Word header with purple gradient
        sb.append("<div class='word-header' style='margin-bottom:8px;background:linear-gradient(135deg,#8b5cf6,#6d28d9);border-radius:16px;padding:16px;box-shadow:0 2px 8px rgba(139,92,246,0.15)'>");
        // Word display with special effects
        sb.append("<div class='word' style='text-align:center'>");
        sb.append("<div style='font-size:32px;font-weight:800;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,0.1)'>")
          .append(escape(englishWord.toUpperCase())).append("</div>");
        
        // Phonetic and part of speech in elegant pills
        sb.append("<div style='display:flex;gap:8px;justify-content:center;margin-top:12px'>");
        if (!safePhon.isEmpty()) {
            sb.append("<div class='phonetic' style='font-size:15px;color:#6d28d9;background:rgba(255,255,255,0.95);padding:4px 12px;border-radius:16px;backdrop-filter:blur(4px)'>")
              .append(escape(safePhon)).append("</div>");
        }
        sb.append("<div class='part-of-speech' style='font-size:15px;color:#6d28d9;background:rgba(255,255,255,0.95);padding:4px 12px;border-radius:16px;backdrop-filter:blur(4px)'>")
          .append(escape(partOfSpeech)).append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");

        // Content grid with beautiful cards
        sb.append("<div class='content-grid' style='display:grid;grid-template-columns:repeat(2,1fr);gap:8px'>");
        
        // Vietnamese meaning card
        sb.append("<div class='meaning-section card' style='padding:12px;border-radius:12px;box-shadow:0 1px 6px rgba(0,0,0,0.05);border:1px solid #e9d5ff'>");
        sb.append("<h3 style='margin:0 0 8px 0;color:#1e293b;font-size:15px;font-weight:600;display:flex;align-items:center;gap:4px'>");
        sb.append("<span style='color:#8b5cf6;font-size:18px'>◆</span>Nghĩa tiếng Việt</h3>");
        sb.append("<div class='content-box' style='color:#334155;font-size:14px;line-height:1.4;padding:8px 10px;background:#faf5ff;border-radius:8px;border:1px solid #f3e8ff'>")
          .append(escape(safeMeaning)).append("</div>");
        sb.append("</div>");

        // Definition card
        sb.append("<div class='definition-section card' style='padding:12px;border-radius:12px;box-shadow:0 1px 6px rgba(0,0,0,0.05);border:1px solid #e9d5ff'>");
        sb.append("<h3 style='margin:0 0 8px 0;color:#1e293b;font-size:15px;font-weight:600;display:flex;align-items:center;gap:4px'>");
        sb.append("<span style='color:#8b5cf6;font-size:18px'>◆</span>Định nghĩa chi tiết</h3>");
        sb.append("<div style='display:flex;flex-direction:column;gap:8px'>");
        for (String line : safeDef.split("\\n")) {
            if (!line.trim().isEmpty()) {
                sb.append("<div class='content-box' style='color:#334155;font-size:14px;line-height:1.4;padding:8px 10px;background:#faf5ff;border-radius:8px;border:1px solid #f3e8ff'>")
                  .append(escape(line)).append("</div>");
            }
        }
        sb.append("</div>");
        sb.append("</div>");

        sb.append("</div>"); // Close content-grid

        // Example section if available (full width)
        if (!safeExample.trim().isEmpty()) {
            sb.append("<div class='example-section card' style='margin-top:8px;padding:12px;border-radius:12px;box-shadow:0 1px 6px rgba(0,0,0,0.05);border:1px solid #e9d5ff'>");
            sb.append("<h3 style='margin:0 0 8px 0;color:#1e293b;font-size:15px;font-weight:600;display:flex;align-items:center;gap:4px'>");
            sb.append("<span style='color:#8b5cf6;font-size:18px'>◆</span>Ví dụ</h3>");
            sb.append("<div class='content-box' style='color:#334155;font-size:14px;line-height:1.4;padding:8px 10px;background:#faf5ff;border-radius:8px;border:1px solid #f3e8ff;font-style:italic'>")
              .append(escape(safeExample)).append("</div>");
            sb.append("</div>");
        }
        
        sb.append("</div>"); // Close word-details
        return wrapHtml(sb.toString());
    }

    private String wrapHtml(String inner) {
        return "<html><head><meta charset='UTF-8'>" +
               "<style>" +
               "body{font-family:'Segoe UI',sans-serif;font-size:14px;margin:0;background:#f5f3ff;color:#1e293b;padding:10px}" +
               ".card{transition:all 0.2s ease}" +
               ".card:hover{transform:translateY(-1px);box-shadow:0 2px 8px rgba(139,92,246,0.1)}" +
               ".content-box{transition:all 0.2s ease}" +
               ".content-box:hover{border-color:#d8b4fe;box-shadow:0 1px 4px rgba(139,92,246,0.06)}" +
               "</style>" +
               "</head><body>" + inner + "</body></html>";
    }

    private String toParagraphHtml(String text) {
        return "<html><head><meta charset='UTF-8'>" +
               "<style>" +
               "body{font-family:'Segoe UI',sans-serif;font-size:15px;margin:0;padding:16px;color:#374151}" +
               "</style>" +
               "</head><body><div style='white-space:pre-wrap'>" + escape(text) + "</div></body></html>";
    }


    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void clearFields() {
        searchField.setText("");
        String html = "<h1 style='margin:0 0 6px 0;color:#333'>TỪ ĐIỂN ANH-VIỆT</h1>" +
                      "<hr style='border:none;height:1px;background:#e5e7eb;margin:6px 0 12px'/>" +
                      "<div style='color:#444;margin-bottom:6px'>Hướng dẫn sử dụng:</div>" +
                      "<ul style='margin:0 0 0 18px;color:#555'>" +
                      "<li>Nhập từ tiếng Anh vào ô tìm kiếm</li>" +
                      "<li>Nhấn 'Tìm kiếm' để tra cứu chính xác</li>" +
                      "<li>Nhấn 'Tìm từ chứa' để tìm các từ có chứa từ khóa</li>" +
                      "<li>Chọn từ trong bảng để xem chi tiết</li>" +
                      "<li>Nhấn 'Làm mới' để xóa kết quả</li>" +
                      "</ul>";
        resultArea.setText(wrapHtml(html));
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
    
