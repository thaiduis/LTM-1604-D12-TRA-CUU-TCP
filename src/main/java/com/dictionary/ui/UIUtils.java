package com.dictionary.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class UIUtils {
    // Modern color scheme
    public static final Color PRIMARY_COLOR = new Color(0, 82, 204);       // Strong blue
    public static final Color SECONDARY_COLOR = new Color(102, 51, 153);   // Deep purple
    public static final Color SUCCESS_COLOR = new Color(0, 135, 90);       // Deep green
    public static final Color BACKGROUND_COLOR = new Color(235, 236, 240); // Light gray
    public static final Color SURFACE_COLOR = new Color(255, 255, 255);    // Pure white
    public static final Color TEXT_PRIMARY = new Color(0, 0, 0);          // Pure black
    public static final Color TEXT_SECONDARY = new Color(66, 66, 66);     // Dark gray
    public static final Color ACCENT_COLOR = new Color(204, 0, 0);        // Deep red
    public static final Color DIVIDER_COLOR = new Color(200, 200, 200);   // Medium gray

    private static boolean darkMode = false;
    private static final int BORDER_RADIUS = 10;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public static void setUIStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Base components
            UIManager.put("Panel.background", SURFACE_COLOR);
            
            // Input fields with distinct styling
            Color inputBackground = new Color(245, 245, 255);  // Light blue-tinted background
            Border inputBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 82, 204, 100), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            );
            
            UIManager.put("TextField.background", inputBackground);
            UIManager.put("TextField.border", inputBorder);
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            
            UIManager.put("TextArea.background", inputBackground);
            UIManager.put("TextArea.border", inputBorder);
            UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("TextArea.foreground", TEXT_PRIMARY);
            
            UIManager.put("ComboBox.background", inputBackground);
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
            
            // Labels and text
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            
            // Tables
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.gridColor", new Color(200, 200, 200));
            UIManager.put("Table.selectionBackground", new Color(0, 82, 204, 40));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            
            // Scrollbars
            UIManager.put("ScrollBar.thumb", new Color(210, 210, 210));
            UIManager.put("ScrollBar.thumbDarkShadow", new Color(210, 210, 210));
            UIManager.put("ScrollBar.thumbHighlight", new Color(210, 210, 210));
            UIManager.put("ScrollBar.thumbShadow", new Color(210, 210, 210));
            UIManager.put("ScrollBar.track", SURFACE_COLOR);
            UIManager.put("ScrollBar.width", 12);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JButton createStyledButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, SECONDARY_COLOR);
    }

    public static JButton createStyledButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    width, height, endColor
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, width, height, BORDER_RADIUS, BORDER_RADIUS));
                
                // Draw slight shadow at bottom
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(2, height - 3, width - 4, 3, 2, 2));
                
                // Draw text with shadow
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int x = (width - textWidth) / 2;
                int y = (height - textHeight) / 2 + fm.getAscent();
                
                // Draw text shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawString(text, x + 1, y + 1);
                
                // Draw text
                g2.setColor(SURFACE_COLOR);
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(size.width + 30, 38);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(endColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(startColor);
            }
        });
        
        return button;
    }

    public static Border createRoundBorder(Color color, int radius) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    public static JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 20),
                    width, height, new Color(SECONDARY_COLOR.getRed(), SECONDARY_COLOR.getGreen(), SECONDARY_COLOR.getBlue(), 10)
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, width, height, BORDER_RADIUS, BORDER_RADIUS));
                
                // Add subtle pattern
                g2.setColor(new Color(255, 255, 255, 40));
                for (int i = 0; i < height; i += 4) {
                    g2.drawLine(0, i, width, i);
                }
                
                g2.dispose();
            }
        };
    }

    public static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (isSelected) {
                c.setBackground(new Color(0, 82, 204, 40));  // Light blue selection
                c.setForeground(new Color(0, 0, 0));         // Black text for selected rows
            } else {
                // Alternate row colors for better readability
                c.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(245, 246, 250));
                c.setForeground(new Color(0, 0, 0));         // Black text for all rows
            }
            
            // Add padding and border for better cell separation
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            
            return c;
        }
    }

    public static class AnimatedPanel extends JPanel {
        private float alpha = 0.0f;
        private Timer fadeInTimer;

        public AnimatedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            
            fadeInTimer = new Timer(30, e -> {
                alpha += 0.08f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    fadeInTimer.stop();
                }
                repaint();
            });
        }

        public void startAnimation() {
            alpha = 0.0f;
            fadeInTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static class SlidePanel extends JPanel {
        private final Timer timer;
        private final JPanel wrapper;
        private int targetHeight;
        private int currentHeight;
        private boolean isExpanded;
        private JButton toggleButton;
        private Component mainContent;

        public SlidePanel() {
            super(new BorderLayout());
            setOpaque(false);
            isExpanded = false;
            currentHeight = 0;

            wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            super.add(wrapper, BorderLayout.CENTER);

            timer = new Timer(16, e -> {
                if (isExpanded) {
                    if (currentHeight < targetHeight) {
                        currentHeight = Math.min(currentHeight + 40, targetHeight);
                        revalidate();
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                    }
                } else {
                    if (currentHeight > 0) {
                        currentHeight = Math.max(currentHeight - 40, 0);
                        revalidate();
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
        }

        @Override
        public Component add(Component comp) {
            if (comp != wrapper) {
                mainContent = comp;
                wrapper.add(comp, BorderLayout.CENTER);
                targetHeight = comp.getPreferredSize().height;
                return comp;
            }
            return super.add(comp);
        }

        @Override
        public void add(Component comp, Object constraints) {
            if (comp != wrapper) {
                mainContent = comp;
                wrapper.add(comp, constraints);
                targetHeight = comp.getPreferredSize().height;
            } else {
                super.add(comp, constraints);
            }
        }

        public void setToggleButton(JButton button) {
            this.toggleButton = button;
            toggleButton.addActionListener(e -> toggle());
        }

        public boolean isShowing() {
            return isExpanded;
        }

        public void toggle() {
            isExpanded = !isExpanded;
            if (mainContent != null) {
                targetHeight = mainContent.getPreferredSize().height;
            }

            if (isExpanded) {
                if (toggleButton != null) {
                    toggleButton.setText("Ẩn form");
                }
            } else {
                if (toggleButton != null) {
                    toggleButton.setText("Hiện form");
                }
            }

            timer.restart();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            if (!isExpanded) {
                size.height = currentHeight;
            }
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }
}