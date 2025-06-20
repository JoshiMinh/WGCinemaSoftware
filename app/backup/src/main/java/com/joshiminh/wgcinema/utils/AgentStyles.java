package com.joshiminh.wgcinema.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.border.Border;

public class AgentStyles {
    // Colors
    public static final Color PRIMARY_BACKGROUND = new Color(25, 25, 25); // Dark background
    public static final Color SECONDARY_BACKGROUND = new Color(40, 40, 40); // Slightly lighter dark background
    public static final Color ACCENT_BLUE = new Color(50, 100, 255); // Bright blue accent
    public static final Color ACCENT_TEAL = new Color(0, 180, 180); // Teal accent
    public static final Color TEXT_COLOR = new Color(240, 240, 240); // Light text
    public static final Color LIGHT_TEXT_COLOR = new Color(180, 180, 180); // Lighter text for secondary info
    public static final Color BORDER_COLOR = new Color(60, 60, 60); // Subtle border color
    public static final Color DANGER_RED = new Color(200, 50, 50); // Red for delete/danger
    public static final Color DANGER_RED_BRIGHTER = new Color(230, 60, 60); // Brighter red on hover
    public static final Color ERROR_COLOR = new Color(231, 76, 60); // A strong red

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Insets/Padding
    public static final Insets LABEL_INSETS = new Insets(8, 0, 8, 10);
    public static final int FORM_PADDING = 30;

    // Utility methods
    public static void applyFrameDefaults(javax.swing.JFrame frame, String title, int width, int height) {
        frame.setTitle(title);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(PRIMARY_BACKGROUND);
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(TITLE_FONT);
        label.setForeground(ACCENT_BLUE);
        return label;
    }

    public static void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    public static void styleComponent(JComponent component) {
        component.setBackground(SECONDARY_BACKGROUND);
        component.setForeground(TEXT_COLOR);
        component.setFont(LABEL_FONT);
    }

    public static Border componentBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );
    }

    /**
     * Parses a Vietnamese currency formatted string (e.g., "80.000vnđ") into a double.
     * It removes "vnđ", thousands separators (dots), and handles decimal commas (if any).
     *
     * @param formattedPrice The price string in Vietnamese currency format.
     * @return The parsed price as a double.
     */
    public static double parseVietnameseCurrency(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.trim().isEmpty()) {
            return 0.0;
        }
        // Remove "vnđ" and trim spaces
        String cleanPrice = formattedPrice.replaceAll("vnđ", "").trim();
        // Remove thousands separators (dots)
        cleanPrice = cleanPrice.replace(".", "");
        // Replace decimal comma with dot (if any, though not expected for whole numbers from formatPrice)
        cleanPrice = cleanPrice.replace(",", ".");
        return Double.parseDouble(cleanPrice);
    }
}
