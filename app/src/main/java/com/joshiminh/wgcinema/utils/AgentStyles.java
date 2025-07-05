package com.joshiminh.wgcinema.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class AgentStyles {

    // --- Color Palette ---
    public static final Color PRIMARY_BACKGROUND = new Color(24, 24, 27); // Zinc 900 - Dark background
    public static final Color SECONDARY_BACKGROUND = new Color(39, 39, 42); // Zinc 800 - Slightly lighter for cards/panels
    public static final Color TEXT_COLOR = new Color(244, 244, 245); // Zinc 50 - Light text for dark backgrounds
    public static final Color LIGHT_TEXT_COLOR = new Color(161, 161, 170); // Zinc 400 - Lighter text for secondary info
    public static final Color ACCENT_BLUE = new Color(37, 99, 235); // Blue 600 - Primary accent color
    public static final Color ACCENT_TEAL = new Color(20, 184, 166); // Teal 500 - Secondary accent color
    public static final Color DANGER_RED = new Color(220, 38, 38); // Red 600 - For delete/danger actions
    public static final Color DANGER_RED_BRIGHTER = new Color(239, 68, 68); // Red 500 - Brighter red for hover
    public static final Color BORDER_COLOR = new Color(63, 63, 70); // Zinc 700 - Subtle border color
    public static final Color ERROR_COLOR = new Color(231, 76, 60); // A strong red, already defined

    // --- Fonts ---
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // --- Spacing & Padding ---
    public static final int FORM_PADDING = 30;
    public static final Insets LABEL_INSETS = new Insets(10, 0, 10, 15); // Padding for labels in forms

    // --- Utility Methods ---

    /**
     * Applies default styling to a JFrame, including background, icon, and basic setup.
     *
     * @param frame The JFrame to style.
     * @param title The title for the frame.
     * @param width The preferred width of the frame.
     * @param height The preferred height of the frame.
     */
    public static void applyFrameDefaults(JFrame frame, String title, int width, int height) {
        frame.setTitle(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(PRIMARY_BACKGROUND);
        frame.setIconImage(ResourceUtil.loadAppIcon());
    }

    /**
     * Creates a styled JLabel for headers.
     *
     * @param text The text for the header label.
     * @return A styled JLabel.
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(TITLE_FONT);
        label.setForeground(ACCENT_BLUE);
        return label;
    }

    /**
     * Applies consistent styling to various JComponents.
     *
     * @param component The JComponent to style.
     */
    public static void styleComponent(JComponent component) {
        component.setBackground(SECONDARY_BACKGROUND);
        component.setForeground(TEXT_COLOR);
        component.setFont(LABEL_FONT);
        if (component instanceof JTextField) {
            ((JTextField) component).setCaretColor(TEXT_COLOR);
        } else if (component instanceof JTextArea) {
            ((JTextArea) component).setCaretColor(TEXT_COLOR);
        } else if (component instanceof JComboBox) {
            ((JComboBox<?>) component).setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? ACCENT_BLUE.darker() : SECONDARY_BACKGROUND);
                    setForeground(TEXT_COLOR);
                    return this;
                }
            });
        }
    }

    /**
     * Creates a consistent border for input components.
     *
     * @return A Border instance.
     */
    public static Border componentBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
    }

    /**
     * Applies consistent styling to JButtons.
     *
     * @param button The JButton to style.
     */
    public static void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_BLUE); // Default button color
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Reset to original color, assuming it's one of the defined constants
                if (button.getBackground().equals(ACCENT_BLUE.brighter())) {
                    button.setBackground(ACCENT_BLUE);
                } else if (button.getBackground().equals(ACCENT_TEAL.brighter())) {
                    button.setBackground(ACCENT_TEAL);
                } else if (button.getBackground().equals(DANGER_RED_BRIGHTER)) {
                    button.setBackground(DANGER_RED);
                }
            }
        });
    }
}