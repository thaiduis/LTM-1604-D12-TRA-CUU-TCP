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
    private JLabel statusLabel;
    
    private AnimatedPanel mainPanel;
    private JButton startStopButton;
    private JButton refreshButton;
    private JButton addWordButton;
    private JButton updateWordButton;
    private JButton deleteWordButton;
    
    private DictionaryFormDialog formDialog;

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
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND_COLOR);

        // Status label
        statusLabel = new JLabel("Server đang dừng", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(UIUtils.ACCENT_COLOR);

        // Server control button
        startStopButton = UIUtils.createStyledButton("▶ Khởi động Server", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));

        // Action buttons
        addWordButton = UIUtils.createStyledButton("Thêm từ mới", UIUtils.SUCCESS_COLOR, new Color(38, 179, 119));
        updateWordButton = UIUtils.createStyledButton("Cập nhật từ", UIUtils.PRIMARY_COLOR, new Color(20, 70, 220));
        deleteWordButton = UIUtils.createStyledButton("Xóa từ", UIUtils.ACCENT_COLOR, new Color(200, 50, 50));

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

        // Refresh button (đưa lên phía trên danh sách)
        refreshButton = UIUtils.createStyledButton("Làm mới", UIUtils.TEXT_SECONDARY, new Color(100, 110, 120));
        refreshButton.addActionListener(e -> refreshWordList());

        mainPanel = new AnimatedPanel(new BorderLayout(20, 20));
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

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setOpaque(false);
        actionPanel.add(addWordButton);
        actionPanel.add(updateWordButton);
        actionPanel.add(deleteWordButton);

        // Layout assembly
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        mainPanel.startAnimation();
    }


    private void setupEventHandlers() {
        startStopButton.addActionListener(e -> toggleServer());
        
        // Action button handlers
        addWordButton.addActionListener(e -> formDialog.showForAdd());
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
