package com.dictionary.ui;

import javax.swing.*;
import java.awt.*;

public class MaterialUIUtils {
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243); // xanh dương
    public static final Color PRIMARY_DARK = new Color(25, 118, 210);
    public static final Color ACCENT_COLOR = new Color(76, 175, 80);   // xanh lá
    public static final Color BACKGROUND = new Color(245, 245, 245);
   
    public static JComboBox<String> createMaterialComboBox(String[] items, Color bgColor, int radius) {
    JComboBox<String> combo = new JComboBox<>(items);

    combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    combo.setForeground(Color.WHITE);
    combo.setOpaque(false);
    combo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    combo.setPreferredSize(new Dimension(160, 40));

    // Custom ô chính
    combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // bỏ nền mặc định
        }
    
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
            // Vẽ nền bo góc
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), radius, radius);
    
            // Vẽ chữ của item đã chọn
            Object selected = combo.getSelectedItem();
            if (selected != null) {
                g2.setColor(Color.WHITE);
                g2.setFont(combo.getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = selected.toString();
    
                int textX = 12;
                int textY = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textX, textY);
            }
    
            // Vẽ mũi tên trắng (không có ô nền)
            int arrowSize = 8;
            int cx = c.getWidth() - 18;
            int cy = c.getHeight() / 2 - 3;
    
            Polygon arrow = new Polygon();
            arrow.addPoint(cx, cy);
            arrow.addPoint(cx + arrowSize, cy);
            arrow.addPoint(cx + arrowSize / 2, cy + arrowSize);
    
            g2.setColor(Color.WHITE);
            g2.fill(arrow);
    
            g2.dispose();
        }
    
        @Override
        protected JButton createArrowButton() {
            // Trả về button trống để không vẽ ô trắng mặc định
            JButton btn = new JButton();
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setOpaque(false);
            return btn;
        }
    });
    
    // Custom dropdown list
    combo.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    
            if (isSelected) {
                label.setBackground(bgColor.darker());
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            }
    
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            return label;
        }
    });
    
    return combo;
}

    
    
    
    

    // Nút Material
    public static JButton createMaterialButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bo góc
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // nền
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);

                // chữ
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.setFont(c.getFont());
                g2.drawString(text, x, y);

                g2.dispose();
            }
        });

        return btn;
    }

    // Search field Material
    public static JTextField createSearchField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    // Card panel Material
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 12, 12);

                // nền trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }
}
