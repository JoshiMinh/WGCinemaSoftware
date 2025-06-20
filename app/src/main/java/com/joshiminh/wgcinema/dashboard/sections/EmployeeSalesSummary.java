package com.joshiminh.wgcinema.dashboard.sections;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

import com.joshiminh.wgcinema.data.DAO;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;
import com.joshiminh.wgcinema.utils.TableStyles;

public class EmployeeSalesSummary {
    private String url;
    private JPanel salesSummaryPanel;
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    public EmployeeSalesSummary(String url) {
        this.url = url;
        this.salesSummaryPanel = new JPanel(new BorderLayout(15, 15));
        this.salesSummaryPanel.setBackground(PRIMARY_BACKGROUND);
        this.salesSummaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = createHeaderLabel("Employee Sales Performance");
        salesSummaryPanel.add(headerLabel, BorderLayout.NORTH);

        setupEmployeeTable();
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.getViewport().setBackground(PRIMARY_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        salesSummaryPanel.add(scrollPane, BorderLayout.CENTER);

        loadEmployeeData();
    }

    public JPanel getSalesSummaryPanel() {
        return salesSummaryPanel;
    }

    private void setupEmployeeTable() {
        String[] columnNames = {"Employee Name", "Email", "Current Month Tickets", "Current Month Revenue", "Last Month Tickets", "Last Month Revenue"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        employeeTable = new JTable(tableModel);
        TableStyles.applyTableStyles(employeeTable);
        employeeTable.setRowHeight(30);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        employeeTable.getTableHeader().setBackground(SECONDARY_BACKGROUND.darker());
        employeeTable.getTableHeader().setForeground(TEXT_COLOR);

        // Custom renderer for the total row
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getModel().getRowCount() - 1 && table.getModel().getValueAt(row, 0).equals("TOTAL")) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD, 15f));
                    c.setBackground(ACCENT_BLUE.darker()); // Highlight total row
                    c.setForeground(TEXT_COLOR);
                } else {
                    c.setBackground(SECONDARY_BACKGROUND);
                    c.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });
    }

    private void loadEmployeeData() {
        tableModel.setRowCount(0); // Clear existing data
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        int grandTotalCurrentTickets = 0;
        double grandTotalCurrentRevenue = 0.0;
        int grandTotalLastTickets = 0;
        double grandTotalLastRevenue = 0.0;

        try (ResultSet employeesRs = DAO.fetchEmployeesWithSalesData(url)) {
            if (employeesRs == null) {
                JOptionPane.showMessageDialog(salesSummaryPanel, "No employee data found or database error.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean foundEmployees = false;
            while (employeesRs.next()) {
                foundEmployees = true;
                String email = employeesRs.getString("account_email");
                String employeeName = employeesRs.getString("name");
                int currentMonthTickets = employeesRs.getInt("total_tickets_sold_current_month");
                double currentMonthRevenue = employeesRs.getDouble("total_revenue_current_month");
                int lastMonthTickets = employeesRs.getInt("total_tickets_sold_last_month");
                double lastMonthRevenue = employeesRs.getDouble("total_revenue_last_month");

                tableModel.addRow(new Object[]{
                        employeeName,
                        email,
                        currentMonthTickets,
                        currencyFormat.format(currentMonthRevenue),
                        lastMonthTickets,
                        currencyFormat.format(lastMonthRevenue)
                });

                // Accumulate grand totals
                grandTotalCurrentTickets += currentMonthTickets;
                grandTotalCurrentRevenue += currentMonthRevenue;
                grandTotalLastTickets += lastMonthTickets;
                grandTotalLastRevenue += lastMonthRevenue;
            }

            if (!foundEmployees) {
                JLabel noEmployeesLabel = new JLabel("No employees found.");
                noEmployeesLabel.setForeground(LIGHT_TEXT_COLOR);
                noEmployeesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noEmployeesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                salesSummaryPanel.add(Box.createVerticalGlue());
                salesSummaryPanel.add(noEmployeesLabel);
                salesSummaryPanel.add(Box.createVerticalGlue());
            } else {
                // Add the total row at the end
                tableModel.addRow(new Object[]{
                        "TOTAL", // Special identifier for the renderer
                        "", // Empty email for total row
                        grandTotalCurrentTickets,
                        currencyFormat.format(grandTotalCurrentRevenue),
                        grandTotalLastTickets,
                        currencyFormat.format(grandTotalLastRevenue)
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(salesSummaryPanel, "Error loading employee data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
