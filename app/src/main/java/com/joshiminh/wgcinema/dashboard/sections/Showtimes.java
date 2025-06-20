package com.joshiminh.wgcinema.dashboard.sections;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.joshiminh.wgcinema.dashboard.agents.ShowtimeAgent;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.*;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

public class Showtimes {
  private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 16);
  private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
  private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);

  private final JPanel showtimesPanel;
  private final String url;
  private JTable showtimesTable;
  private DefaultTableModel showtimesTableModel;

  public Showtimes(String url) {
      this.url = url;
      showtimesPanel = new JPanel(new BorderLayout());
      showtimesPanel.setBackground(PRIMARY_BACKGROUND);

      JPanel titlePanel = new JPanel(new BorderLayout());
      titlePanel.setBackground(SECONDARY_BACKGROUND);

      JLabel titleLabel = new JLabel("Show Times", SwingConstants.CENTER);
      titleLabel.setForeground(ACCENT_BLUE);
      titleLabel.setFont(TITLE_FONT);
      titleLabel.setBorder(new EmptyBorder(16, 0, 16, 0));
      titlePanel.add(titleLabel, BorderLayout.CENTER);

      JButton newButton = new JButton("New");
      newButton.setFocusPainted(false);
      newButton.setBackground(ACCENT_TEAL);
      newButton.setForeground(TEXT_COLOR);
      newButton.setFont(BUTTON_FONT);
      newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      newButton.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(ACCENT_TEAL.darker(), 2, true),
              BorderFactory.createEmptyBorder(10, 22, 10, 22)
      ));
      newButton.addActionListener(_ -> {
          new ShowtimeAgent(url, () -> refreshShowtimesPanel()).setVisible(true);
      });
      newButton.addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseEntered(java.awt.event.MouseEvent e) {
              newButton.setBackground(ACCENT_TEAL.brighter());
          }
          public void mouseExited(java.awt.event.MouseEvent e) {
              newButton.setBackground(ACCENT_TEAL);
          }
      });
      titlePanel.add(newButton, BorderLayout.EAST);

      showtimesPanel.add(titlePanel, BorderLayout.NORTH);

      showtimesTableModel = new DefaultTableModel() {
          @Override
          public boolean isCellEditable(int row, int column) {
              return column != 0;
          }
      };
      showtimesTable = new JTable(showtimesTableModel);

      showtimesTable.setFont(TABLE_FONT);
      showtimesTable.setForeground(TEXT_COLOR);
      showtimesTable.setBackground(SECONDARY_BACKGROUND);
      showtimesTable.setRowHeight(32);
      showtimesTable.setGridColor(BORDER_COLOR);
      showtimesTable.setFillsViewportHeight(true);
      showtimesTable.setIntercellSpacing(new Dimension(0, 0));
      showtimesTable.setSelectionBackground(ACCENT_BLUE.darker());
      showtimesTable.setSelectionForeground(TEXT_COLOR);
      showtimesTable.setShowHorizontalLines(true);
      showtimesTable.setShowVerticalLines(false);

      JTableHeader header = showtimesTable.getTableHeader();
      header.setFont(HEADER_FONT);
      header.setBackground(SECONDARY_BACKGROUND);
      header.setForeground(ACCENT_BLUE);
      header.setReorderingAllowed(false);
      header.setResizingAllowed(false);

      loadTableData(true);

      showtimesTable.getModel().addTableModelListener(e -> {
          if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
              int row = e.getFirstRow();
              int column = e.getColumn();
              if (column != showtimesTable.getColumnCount() - 1) {
                  Object updatedValue = showtimesTable.getValueAt(row, column);
                  Object idValue = showtimesTable.getValueAt(row, 0);
                  DAO.updateShowtimeColumn(url, showtimesTable.getColumnName(column), updatedValue, idValue);
              }
          }
      });

      showtimesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
      showtimesTable.getColumn("Actions").setCellEditor(new ButtonEditor(url, showtimesTable, "showtimes", "showtime_id", () -> refreshShowtimesPanel()));

      showtimesTable.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
              int column = showtimesTable.columnAtPoint(e.getPoint());
              int row = showtimesTable.rowAtPoint(e.getPoint());
              if (column == showtimesTable.getColumnCount() - 1) { 
                  if (showtimesTable.isCellEditable(row, column)) {
                      showtimesTable.editCellAt(row, column);
                      Object editorComponent = showtimesTable.getEditorComponent();
                      if (editorComponent instanceof JButton) {
                          ((JButton) editorComponent).doClick();
                      }
                  }
              }
          }
      });


      JScrollPane scrollPane = new JScrollPane(showtimesTable);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      scrollPane.setBackground(PRIMARY_BACKGROUND);
      scrollPane.getViewport().setBackground(PRIMARY_BACKGROUND);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      showtimesPanel.add(scrollPane, BorderLayout.CENTER);
  }

  private void loadTableData(boolean isInitialLoad) {
      showtimesTableModel.setRowCount(0);

      if (isInitialLoad) {
          showtimesTableModel.setColumnCount(0);
          try (ResultSet resultSet = DAO.fetchAllShowtimes(url)) {
              ResultSetMetaData metaData = resultSet.getMetaData();
              int columnCount = metaData.getColumnCount();
              for (int i = 1; i <= columnCount; i++) {
                  showtimesTableModel.addColumn(metaData.getColumnLabel(i));
              }
              showtimesTableModel.addColumn("Actions");
          } catch (SQLException e) {
              JOptionPane.showMessageDialog(null, "Error loading showtimes columns: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
      }

      try (ResultSet resultSet = DAO.fetchAllShowtimes(url)) {
          int columnCountInModel = showtimesTableModel.getColumnCount();
          while (resultSet.next()) {
              Object[] rowData = new Object[columnCountInModel];
              for (int i = 1; i < columnCountInModel; i++) {
                  rowData[i - 1] = resultSet.getObject(i);
              }
              rowData[columnCountInModel - 1] = "Delete";
              showtimesTableModel.addRow(rowData);
          }
      } catch (SQLException e) {
          JOptionPane.showMessageDialog(null, "Error loading showtimes data rows: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
  }

  private void refreshShowtimesPanel() {
      loadTableData(false);
      showtimesPanel.revalidate();
      showtimesPanel.repaint();
  }

  public JPanel getShowtimesPanel() {
      return showtimesPanel;
  }
}
