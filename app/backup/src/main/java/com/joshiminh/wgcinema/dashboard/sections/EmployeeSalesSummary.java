package com.joshiminh.wgcinema.dashboard.sections;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

        JLabel headerLabel = createHeaderLabel("Employee Sales Performance"); // Changed header
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
        String[] columnNames = {"Employee Name", "Email", "Total Tickets Sold", "Total Revenue"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        employeeTable = new JTable(tableModel);
        TableStyles.applyTableStyles(employeeTable); // Apply common table styles
        employeeTable.setRowHeight(30);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        employeeTable.getTableHeader().setBackground(SECONDARY_BACKGROUND.darker());
        employeeTable.getTableHeader().setForeground(TEXT_COLOR);
    }

    private void loadEmployeeData() {
        // Call the monthly sales reset method before loading data
        DAO.resetMonthlySales(url); // <--- Thêm dòng này

        tableModel.setRowCount(0); // Clear existing data
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // For Vietnamese currency

        try (ResultSet employeesRs = DAO.fetchEmployees(url)) {
            if (employeesRs == null) {
                JOptionPane.showMessageDialog(salesSummaryPanel, "No employee data found or database error.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            while (employeesRs.next()) {
                String email = employeesRs.getString("account_email");
                String employeeName = employeesRs.getString("name");

                // Lấy dữ liệu doanh số từ các cột mới trong bảng accounts
                int totalTicketsSoldCurrentMonth = employeesRs.getInt("total_tickets_sold_current_month");
                double totalRevenueCurrentMonth = employeesRs.getDouble("total_revenue_current_month");

                tableModel.addRow(new Object[]{
                        employeeName,
                        email,
                        totalTicketsSoldCurrentMonth,
                        currencyFormat.format(totalRevenueCurrentMonth)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(salesSummaryPanel, "Error loading employee data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
