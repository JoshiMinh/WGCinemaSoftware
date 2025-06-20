package com.joshiminh.wgcinema.utils;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

import com.joshiminh.wgcinema.data.DAO;

/**
* ButtonEditor is a custom TableCellEditor that provides a "Delete" button
* for each row in a JTable. When clicked, it deletes the corresponding record
* from the database and removes the row from the table.
*/
@SuppressWarnings("unused")
public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
   private final JButton button; // The "Delete" button
   private final JTable table; // The JTable this editor is associated with
   private final String url; // Database connection URL
   private final String tableName; // Name of the database table
   private final String primaryKeyColumn; // Primary key column name in the database table
   private int row; // The row index of the button being edited
   private final Runnable refreshCallback; // Thêm callback để làm mới UI

   /**
    * Constructor to initialize the ButtonEditor.
    *
    * @param url              Database connection URL
    * @param table            JTable instance
    * @param tableName        Name of the database table
    * @param primaryKeyColumn Primary key column name in the database table
    * @param refreshCallback  A Runnable to be executed after successful deletion to refresh the UI.
    */
   public ButtonEditor(String url, JTable table, String tableName, String primaryKeyColumn, Runnable refreshCallback) {
       this.url = url;
       this.table = table;
       this.tableName = tableName;
       this.primaryKeyColumn = primaryKeyColumn;
       this.refreshCallback = refreshCallback; // Gán callback

       // Initialize the "Delete" button
       button = new JButton("Delete");
       button.setOpaque(true);
       button.setBackground(Color.RED);
       button.setForeground(Color.WHITE);

       // Add an ActionListener to handle the delete action
       button.addActionListener(e -> {
           fireEditingStopped(); // Stop editing before performing the action
           int confirmation = JOptionPane.showConfirmDialog(
               null, 
               "Are you sure you want to delete this record?", 
               "Delete Confirmation", 
               JOptionPane.YES_NO_OPTION, 
               JOptionPane.WARNING_MESSAGE
           );
           if (confirmation == JOptionPane.YES_OPTION) {
               deleteRow(); // Perform the delete operation
           }
       });
   }

   /**
    * Deletes the row from the database and removes it from the JTable.
    */
   private void deleteRow() {
       // Get the primary key value of the row to be deleted
       int id = (int) table.getValueAt(row, 0);
       System.out.println("ButtonEditor: Deleting row with ID = " + id + " from table " + tableName);

       // Call the DAO method to delete the row from the database
       int result = DAO.deleteRowById(url, tableName, primaryKeyColumn, id);
       System.out.println("ButtonEditor: DAO.deleteRowById returned " + result + " rows affected.");

       // Handle the result of the delete operation
       if (result > 0) {
           JOptionPane.showMessageDialog(null, "Deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
           if (refreshCallback != null) {
               refreshCallback.run(); // Gọi callback để làm mới toàn bộ UI
           }
       } else {
           JOptionPane.showMessageDialog(null, "Error deleting record. No rows affected or database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
       }
   }

   /**
    * Returns the component used for editing the cell.
    *
    * @param table     The JTable that is asking the editor to edit
    * @param value     The value of the cell to be edited
    * @param isSelected Whether the cell is selected
    * @param row       The row index of the cell being edited
    * @param column    The column index of the cell being edited
    * @return The component used for editing
    */
   @Override
   public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
       this.row = row; // Store the row index for later use
       return button; // Return the "Delete" button as the editor component
   }

   /**
    * Returns the value contained in the editor.
    *
    * @return The value of the cell editor
    */
   @Override
   public Object getCellEditorValue() {
       return "Delete"; // Return the button label as the editor value
   }
}
