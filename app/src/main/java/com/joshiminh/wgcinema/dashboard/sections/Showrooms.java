package com.joshiminh.wgcinema.dashboard.sections;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.joshiminh.wgcinema.dashboard.agents.ShowroomAgent;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

public class Showrooms {
 private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 15);
 private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

 private final JPanel showroomsPanel;
 private final String url;
 private JTable showroomsTable;
 private DefaultTableModel showroomsTableModel;
 private ChartPanel currentChartPanel;

 public Showrooms(String url) {
     this.url = url;
     showroomsPanel = new JPanel(new BorderLayout(0, 12));
     showroomsPanel.setBackground(PRIMARY_BACKGROUND);
     showroomsPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
     showroomsPanel.add(createTitlePanel(url), BorderLayout.NORTH);
     showroomsPanel.add(createTablePanel(url), BorderLayout.CENTER);
     currentChartPanel = createChartPanel(url);
     showroomsPanel.add(currentChartPanel, BorderLayout.SOUTH);
 }

 private JPanel createTitlePanel(String url) {
     JPanel titlePanel = new JPanel(new BorderLayout());
     titlePanel.setBackground(PRIMARY_BACKGROUND);

     JLabel titleLabel = new JLabel("Show Rooms", SwingConstants.LEFT);
     titleLabel.setForeground(ACCENT_BLUE);
     titleLabel.setFont(TITLE_FONT);
     titleLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
     titlePanel.add(titleLabel, BorderLayout.WEST);

     JButton newButton = new JButton("New");
     newButton.setFont(BUTTON_FONT);
     newButton.setBackground(ACCENT_TEAL);
     newButton.setForeground(TEXT_COLOR);
     newButton.setFocusPainted(false);
     newButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
     newButton.addActionListener(e -> {
         // Truyền callback để làm mới bảng và biểu đồ sau khi thêm thành công
         new ShowroomAgent(url, () -> refreshShowroomsPanel()).setVisible(true);
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

     return titlePanel;
 }

 private JPanel createTablePanel(String url) {
     showroomsTable = createTable(url);
     JScrollPane scrollPane = new JScrollPane(showroomsTable);
     scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
     scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     scrollPane.setBorder(BorderFactory.createEmptyBorder());
     scrollPane.getViewport().setBackground(PRIMARY_BACKGROUND);

     JPanel panel = new JPanel(new BorderLayout());
     panel.setBackground(PRIMARY_BACKGROUND);
     panel.add(scrollPane, BorderLayout.CENTER);
     return panel;
 }

 private JTable createTable(String url) {
     showroomsTableModel = new DefaultTableModel() {
         @Override
         public boolean isCellEditable(int row, int intcolumn) {
             return intcolumn != 0;
         }
     };

     JTable table = new JTable(showroomsTableModel);
     TableStyles.applyTableStyles(table); // Corrected: Use static method
     table.setFont(TABLE_FONT);
     table.setRowHeight(32);
     table.setForeground(TEXT_COLOR);
     table.setBackground(SECONDARY_BACKGROUND);
     table.setSelectionBackground(ACCENT_BLUE.darker());
     table.setSelectionForeground(TEXT_COLOR);
     table.getTableHeader().setFont(TABLE_FONT.deriveFont(Font.BOLD));
     table.getTableHeader().setBackground(PRIMARY_BACKGROUND);
     table.getTableHeader().setForeground(ACCENT_BLUE);

     DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer();
     paddedRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));
     paddedRenderer.setForeground(TEXT_COLOR);
     paddedRenderer.setBackground(SECONDARY_BACKGROUND);
     
     loadTableData(table, showroomsTableModel, url, true);

     table.getColumn("Actions").setCellRenderer(new ButtonRenderer());
     table.getColumn("Actions").setCellEditor(new ButtonEditor(url, table, "showrooms", "showroom_id", () -> refreshShowroomsPanel()));

     // Add TableModelListener to save changes to the database
     showroomsTableModel.addTableModelListener(e -> {
         if (e.getType() == TableModelEvent.UPDATE) {
             int row = e.getFirstRow();
             int column = e.getColumn();
             // Ensure the edited column is not the "Actions" column
             if (column != showroomsTableModel.getColumnCount() - 1) {
                 updateTableCell(url, table, row, column);
             }
         }
     });

     table.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
             int column = table.columnAtPoint(e.getPoint());
             int row = table.rowAtPoint(e.getPoint());
             if (column == table.getColumnCount() - 1) { 
                 if (table.isCellEditable(row, column)) {
                     table.editCellAt(row, column);
                     Object editorComponent = table.getEditorComponent();
                     if (editorComponent instanceof JButton) {
                         ((JButton) editorComponent).doClick();
                     }
                 }
             }
         }
     });

     for (int i = 0; i < table.getColumnCount(); i++) {
         if (!table.getColumnName(i).equals("Actions")) {
             table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
         }
     }

     return table;
 }

 private void loadTableData(JTable table, DefaultTableModel model, String url, boolean isInitialLoad) {
     model.setRowCount(0);

     if (isInitialLoad) {
         model.setColumnCount(0);
         try (ResultSet resultSet = DAO.fetchAllShowrooms(url)) {
             ResultSetMetaData metaData = resultSet.getMetaData();
             int columnCount = metaData.getColumnCount();
             for (int i = 1; i <= columnCount; i++) {
                 model.addColumn(metaData.getColumnLabel(i));
             }
             model.addColumn("Actions");
         } catch (SQLException e) {
             showError("Error loading showrooms columns: " + e.getMessage());
         }
     }

     try (ResultSet resultSet = DAO.fetchAllShowrooms(url)) {
         int columnCountInModel = model.getColumnCount();
         while (resultSet.next()) {
             Object[] rowData = new Object[columnCountInModel];
             for (int i = 1; i < columnCountInModel; i++) {
                 rowData[i - 1] = resultSet.getObject(i);
             }
             rowData[columnCountInModel - 1] = "Delete";
             model.addRow(rowData);
         }
     } catch (SQLException e) {
         showError("Error loading showrooms data rows: " + e.getMessage());
     }
 }

 private void updateTableCell(String url, JTable table, int row, int column) {
     Object updatedValue = table.getValueAt(row, column);
     Object idValue = table.getValueAt(row, 0);
     DAO.updateShowroomColumn(url, table.getColumnName(column), updatedValue, idValue);
 }

 private ChartPanel createChartPanel(String url) {
     DefaultCategoryDataset dataset = new DefaultCategoryDataset();
     populateChartDataset(dataset, url);

     JFreeChart barChart = ChartFactory.createBarChart(
             "Seats per Showroom", "Showroom", "Number of Seats",
             dataset, PlotOrientation.VERTICAL, false, true, false);

     styleChart(barChart);

     ChartPanel chartPanel = new ChartPanel(barChart);
     chartPanel.setPreferredSize(new Dimension(420, 260));
     chartPanel.setBackground(PRIMARY_BACKGROUND);
     chartPanel.setMouseWheelEnabled(true);
     chartPanel.setDomainZoomable(true);
     chartPanel.setRangeZoomable(true);

     return chartPanel;
 }

 private void populateChartDataset(DefaultCategoryDataset dataset, String url) {
     dataset.clear();
     try (ResultSet resultSet = DAO.fetchAllShowrooms(url)) {
         while (resultSet.next()) {
             dataset.addValue(resultSet.getInt("max_chairs"), "Seats", "Room " + resultSet.getInt("showroom_id"));
         }
     } catch (SQLException e) {
         showError("Error loading chart data: " + e.getMessage());
     }
 }

 private void styleChart(JFreeChart chart) {
     chart.getTitle().setPaint(ACCENT_BLUE);
     chart.setBackgroundPaint(PRIMARY_BACKGROUND);

     CategoryPlot plot = chart.getCategoryPlot();
     plot.setBackgroundPaint(SECONDARY_BACKGROUND);
     plot.setOutlinePaint(BORDER_COLOR);
     plot.setRangeGridlinePaint(LIGHT_TEXT_COLOR);
     plot.getDomainAxis().setLabelPaint(ACCENT_BLUE);
     plot.getDomainAxis().setTickLabelPaint(TEXT_COLOR);
     plot.getRangeAxis().setLabelPaint(ACCENT_BLUE);
     plot.getRangeAxis().setTickLabelPaint(TEXT_COLOR);
     
     plot.getRenderer().setSeriesPaint(0, ACCENT_TEAL);
 }

 private void showError(String message) {
     JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
 }

 private void refreshShowroomsPanel() {
     loadTableData(showroomsTable, showroomsTableModel, url, false); 

     DefaultCategoryDataset dataset = (DefaultCategoryDataset) currentChartPanel.getChart().getCategoryPlot().getDataset();
     populateChartDataset(dataset, url);
     
     showroomsPanel.revalidate();
     showroomsPanel.repaint();
 }

 public JPanel getShowroomsPanel() {
     return showroomsPanel;
 }
}
