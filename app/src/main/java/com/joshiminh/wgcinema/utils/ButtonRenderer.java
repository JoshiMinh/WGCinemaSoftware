package com.joshiminh.wgcinema.utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * ButtonRenderer is a custom TableCellRenderer that renders a button in a JTable cell.
 * This class extends JButton and implements TableCellRenderer to provide a button
 * with custom styling and behavior for table cells.
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {

    /**
     * Constructor to initialize the button with default styles.
     */
    public ButtonRenderer() {
        // Make the button opaque to ensure background color is visible
        setOpaque(true);

        // Set default background and foreground colors
        setBackground(Color.RED);
        setForeground(Color.WHITE);
    }

    /**
     * Returns the component used for rendering the cell.
     *
     * @param table     The JTable that uses this renderer.
     * @param value     The value to assign to the cell.
     * @param isSelected Whether the cell is selected.
     * @param hasFocus  Whether the cell has focus.
     * @param row       The row index of the cell.
     * @param column    The column index of the cell.
     * @return The component used to render the cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Set the button text to the cell value or a default label if null
        setText(value == null ? "Delete" : value.toString());
        return this;
    }
}
