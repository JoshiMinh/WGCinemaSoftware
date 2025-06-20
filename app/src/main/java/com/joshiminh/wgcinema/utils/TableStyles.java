package com.joshiminh.wgcinema.utils;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTable;

/**
 * Utility class to apply consistent styles to JTable components.
 */
public class TableStyles {

    // Background color for the table
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);

    /**
     * Private constructor to prevent instantiation, as this is a utility class.
     */
    private TableStyles() {
        // Private constructor
    }

    /**
     * Applies predefined styles to the given JTable.
     *
     * @param table The JTable to style.
     */
    public static void applyTableStyles(JTable table) {
        // Enable viewport height filling for the table
        table.setFillsViewportHeight(true);

        // Set table background and foreground colors
        table.setBackground(BACKGROUND_COLOR);
        table.setForeground(Color.WHITE);

        // Set table font for cell content
        table.setFont(new Font("Arial", Font.PLAIN, 12));

        // Style the table header
        table.getTableHeader().setBackground(Color.DARK_GRAY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    }
}
