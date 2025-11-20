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

    
    
    
    

    // Nút Material với hover effect
    public static JButton createMaterialButton(String text, Color bg) {
        final boolean[] hovered = {false};
        
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 42));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered[0] = true;
                btn.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered[0] = false;
                btn.repaint();
            }
        });

        // Bo góc với gradient và shadow
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow khi hover
                if (hovered[0]) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(2, 3, c.getWidth() - 4, c.getHeight() - 4, 10, 10);
                }

                // Gradient background
                Color darkColor = bg.darker();
                GradientPaint gp = new GradientPaint(
                    0, 0, hovered[0] ? bg.brighter() : bg,
                    0, c.getHeight(), hovered[0] ? darkColor : darkColor.darker()
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, c.getWidth() - 2, c.getHeight() - 2, 10, 10);

                // Chữ với shadow
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Text shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.setFont(c.getFont());
                g2.drawString(text, x + 1, y + 1);
                
                // Text
                g2.setColor(Color.WHITE);
                g2.drawString(text, x, y);

                g2.dispose();
            }
        });

        return btn;
    }

    // Search field Material với focus effect
    public static JTextField createSearchField() {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background với gradient nhẹ
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(250, 251, 255),
                    0, getHeight(), Color.WHITE
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(new Color(33, 33, 33));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(300, 45));
        
        // Border thay đổi khi focus
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        
        // Focus listener để thay đổi border
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 3, true),
                    BorderFactory.createEmptyBorder(9, 16, 9, 16)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)
                ));
            }
        });
        
        return field;
    }

    // Card panel Material với multi-layer shadow
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Multi-layer shadow cho hiệu ứng depth
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 16, 16);
                
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 16, 16);
                
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);

                // Nền trắng với gradient nhẹ
                GradientPaint gp = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, getHeight(), new Color(250, 251, 255)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }
}
