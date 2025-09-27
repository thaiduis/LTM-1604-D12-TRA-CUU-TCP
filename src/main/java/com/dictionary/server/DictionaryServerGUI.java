package com.dictionary.server;

import com.dictionary.model.Word;
import com.dictionary.database.DictionaryDAO;
import com.dictionary.ui.UIUtils;
import com.dictionary.ui.UIUtils.AnimatedPanel;
import com.dictionary.ui.UIUtils.ModernTableCellRenderer;

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
    private JTable activityTable;
    private DefaultTableModel activityLogModel;
    private JLabel statusLabel;
    private JLabel totalWordsLabel;

    private AnimatedPanel mainPanel;
    private JButton startStopButton;
    private JButton refreshButton;
    private JButton addWordButton;
    private JButton updateWordButton;
    private JButton deleteWordButton;
    private JButton clearHistoryButton;

    private DictionaryFormDialog formDialog;

    // Trong class DictionaryServerGUI
private static final String LOG_FILE = "activity_log.csv";

public void logActivity(String action, String detail) {   // 🔹 đổi private → public
    String time = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // Ghi vào bảng log (thêm lên đầu)
    activityLogModel.insertRow(0, new Object[]{ time, action, detail });

    // Ghi nối (append) vào CSV
    try (java.io.FileWriter fw = new java.io.FileWriter(LOG_FILE, true)) {
        fw.write(String.format("\"%s\",\"%s\",\"%s\"%n", time, action, detail));
    } catch (java.io.IOException ex) {
        ex.printStackTrace();
    }
}

public void clearActivityLog() {
    // Xóa dữ liệu khỏi bảng
    activityLogModel.setRowCount(0);
    
    // Xóa nội dung file CSV
    try (java.io.FileWriter fw = new java.io.FileWriter(LOG_FILE, false)) {
        // Ghi header cho file CSV
        fw.write("\"Thời gian\",\"Hành động\",\"Chi tiết\"\n");
    } catch (java.io.IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Không thể xóa lịch sử hoạt động: " + ex.getMessage(),
            "Lỗi", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Ghi lại hoạt động xóa lịch sử
    logActivity("Quản trị", "Xóa lịch sử hoạt động");
}


    public DictionaryServerGUI() {
        this.dictionaryDAO = new DictionaryDAO();
        this.formDialog = new DictionaryFormDialog(this, dictionaryDAO);
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
        setSize(1300, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);
    
        // Status label
        statusLabel = new JLabel("Server đang dừng", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(UIUtils.ACCENT_COLOR);
        
        // Total words label
        totalWordsLabel = new JLabel("Tổng số từ: 0", SwingConstants.CENTER);
        totalWordsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalWordsLabel.setForeground(UIUtils.PRIMARY_COLOR);
    
        // Server control button
        startStopButton = UIUtils.createStyledButton("▶ Khởi động Server", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
    
        // Action buttons
        addWordButton = UIUtils.createStyledButton("Thêm từ mới", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        updateWordButton = UIUtils.createStyledButton("Cập nhật từ", UIUtils.PRIMARY_COLOR, new Color(20, 70, 220));
        deleteWordButton = UIUtils.createStyledButton("Xóa từ", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));
    
        // Initialize table từ điển
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
        wordTable.setGridColor(new Color(200, 200, 200));
        wordTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        wordTable.getTableHeader().setBackground(new Color(240, 242, 245));
        wordTable.getTableHeader().setForeground(new Color(0, 0, 0));
    
        TableColumnModel columnModel = wordTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(300);
        columnModel.getColumn(5).setPreferredWidth(250);
    
        // Refresh button
        refreshButton = UIUtils.createStyledButton("Làm mới", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
        refreshButton.addActionListener(e -> refreshWordList());
        
        // Clear history button
        clearHistoryButton = UIUtils.createStyledButton("Xóa lịch sử", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));
    
        // Activity log table
        String[] logColumns = {"Thời gian", "Hành động", "Chi tiết"};
        activityLogModel = new DefaultTableModel(logColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        activityTable = new JTable(activityLogModel);
        activityTable.setRowHeight(30);
        activityTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        activityTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
    
        mainPanel = new AnimatedPanel(new BorderLayout(20, 20));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setOpaque(false);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setOpaque(false);
        controlPanel.add(statusLabel);
        controlPanel.add(startStopButton);
        headerPanel.add(controlPanel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(totalWordsLabel);
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
    
        // Tabbed pane chứa Dictionary và Activity log
        // Tabbed pane chứa Dictionary và Activity log
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                setFont(new Font("Segoe UI", Font.BOLD, 15)); // chữ to hơn
            }
        };
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            private final int TAB_HEIGHT = 40;

            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets = new Insets(5, 10, 5, 10);
                contentBorderInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(new Color(33, 150, 243)); // xanh dương
                } else {
                    g2.setColor(new Color(245, 245, 245)); // nền sáng nhạt
                }

                g2.fillRoundRect(x, y + 5, w, h - 5, 12, 12); // tab bo tròn mềm
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                                        int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // bỏ viền đen cũ => cho gọn
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement,
                                            int selectedIndex) {
                // bỏ viền content => để tab trông phẳng
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                    FontMetrics metrics, int tabIndex,
                                    String title, Rectangle textRect,
                                    boolean isSelected) {
                g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (isSelected) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(new Color(80, 80, 80));
                }
                // Lấy bounds của tab
    Rectangle tabRect = getTabBounds(tabIndex, new Rectangle());

    // Tính vị trí để chữ nằm giữa ô tab
    int textWidth = metrics.stringWidth(title);
    int textHeight = metrics.getHeight();

    int x = tabRect.x + (tabRect.width - textWidth) / 2;
    int y = tabRect.y + (tabRect.height - textHeight) / 2 + metrics.getAscent();

    g.drawString(title, x, y);
                
               
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return TAB_HEIGHT;
            }
        });

        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

    
        // Dictionary panel
        JPanel tablePanel = new JPanel(new BorderLayout(20, 20));
        tablePanel.setOpaque(false);
    
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Từ điển", SwingConstants.LEFT);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(refreshButton);
        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(rightHeader, BorderLayout.EAST);
    
        JScrollPane tableScroll = new JScrollPane(wordTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIUtils.DIVIDER_COLOR));
    
        tablePanel.add(tableHeader, BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
    
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);
        actionPanel.add(addWordButton);
        actionPanel.add(updateWordButton);
        actionPanel.add(deleteWordButton);
    
        tablePanel.add(actionPanel, BorderLayout.SOUTH);
    
        // Activity log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        
        // Log header with title only
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        JLabel logLabel = new JLabel("Lịch sử hoạt động", SwingConstants.LEFT);
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logHeader.add(logLabel, BorderLayout.WEST);
        
        JScrollPane logScroll = new JScrollPane(activityTable);
        logScroll.setBorder(BorderFactory.createLineBorder(UIUtils.DIVIDER_COLOR));
        
        // Bottom panel with clear button in bottom right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearHistoryButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);
        logPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        tabbedPane.addTab("Quản lý từ điển", tablePanel);
        tabbedPane.addTab("Lịch sử hoạt động", logPanel);
    
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    

    private void setupEventHandlers() {
        startStopButton.addActionListener(e -> toggleServer());

        addWordButton.addActionListener(e -> {
            formDialog.showForAdd();
        });

        updateWordButton.addActionListener(e -> {
            int selectedRow = wordTable.getSelectedRow();
            if (selectedRow >= 0) {
                Word selectedWord = getWordFromTable(selectedRow);
                formDialog.showForUpdate(selectedWord);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn từ cần cập nhật!");
            }
        });

        deleteWordButton.addActionListener(e -> {
            int selectedRow = wordTable.getSelectedRow();
            if (selectedRow >= 0) {
                Word selectedWord = getWordFromTable(selectedRow);
                formDialog.showForDelete(selectedWord);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn từ cần xóa!");
            }
        });

        clearHistoryButton.addActionListener(e -> {
            int confirmResult = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa toàn bộ lịch sử hoạt động?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa lịch sử",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmResult == JOptionPane.YES_OPTION) {
                clearActivityLog();
            }
        });
    }


    private Word getWordFromTable(int row) {
        return new Word(
                (String) tableModel.getValueAt(row, 0),
                (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2),
                (String) tableModel.getValueAt(row, 3),
                (String) tableModel.getValueAt(row, 4),
                (String) tableModel.getValueAt(row, 5)
        );
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
            logActivity("Server", "Khởi động server");
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
            logActivity("Server", "Dừng server");
        }
    }

    public void refreshWordList() {
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
        // Cập nhật tổng số từ
        totalWordsLabel.setText("Tổng số từ: " + words.size());
    }

    @Override
    public void dispose() {
        stopServer();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DictionaryServerGUI::new);
    }
}
