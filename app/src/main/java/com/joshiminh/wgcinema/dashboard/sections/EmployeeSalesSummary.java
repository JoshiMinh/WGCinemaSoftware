package com.joshiminh.wgcinema.dashboard.sections;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.joshiminh.wgcinema.utils.AgentStyles.*;
import com.joshiminh.wgcinema.utils.TableStyles;

public class EmployeeSalesSummary {
    private String url;
    private JPanel salesSummaryPanel;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private String dbUrl = "jdbc:mysql://localhost:8000/cinema_data"; // Cập nhật theo DB_HOST và DB_NAME
    private String dbUser = "root"; // Cập nhật theo DB_USERNAME
    private String dbPassword = "123456"; // Cập nhật theo DB_PASSWORD

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

        // Map to store statistics for each email
        Map<String, int[]> employeeStats = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT account_email, transaction_date, seats_preserved, amount FROM transactions")) {

            if (rs == null) {
                JOptionPane.showMessageDialog(salesSummaryPanel, "Không tìm thấy dữ liệu giao dịch hoặc lỗi cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate currentDate = LocalDate.now(); // 2025-06-24
            int currentMonth = currentDate.getMonthValue(); // 6
            int currentYear = currentDate.getYear(); // 2025
            int lastMonth = currentMonth == 1 ? 12 : currentMonth - 1; // 5
            int lastYear = currentMonth == 1 ? currentYear - 1 : currentYear;

            while (rs.next()) {
                String email = rs.getString("account_email");
                String transactionDateStr = rs.getString("transaction_date");
                String seatsStr = rs.getString("seats_preserved");
                double amount = rs.getDouble("amount");

                // Parse transaction date (extract date part, e.g., 2025-05-24)
                LocalDate transDate = LocalDate.parse(transactionDateStr.split(" ")[0]);
                int transMonth = transDate.getMonthValue();
                int transYear = transDate.getYear();
                boolean isCurrentMonth = transYear == currentYear && transMonth == currentMonth;
                boolean isLastMonth = transYear == lastYear && transMonth == lastMonth;

                // Count seats
                int seats = seatsStr != null ? seatsStr.split(",").length : 0;

                employeeStats.computeIfAbsent(email, k -> new int[]{0, 0, 0, 0}); // [currentTickets, currentRevenue, lastTickets, lastRevenue]
                int[] stats = employeeStats.get(email);
                if (isCurrentMonth) {
                    stats[0] += seats;
                    stats[1] += amount;
                } else if (isLastMonth) {
                    stats[2] += seats;
                    stats[3] += amount;
                }
            }

            // Add rows for each employee
            for (Map.Entry<String, int[]> entry : employeeStats.entrySet()) {
                String email = entry.getKey();
                int[] stats = entry.getValue();
                tableModel.addRow(new Object[]{
                        getEmployeeName(email), // Get employee name based on email
                        email,
                        stats[0], // Current Month Tickets
                        currencyFormat.format(stats[1]), // Current Month Revenue
                        stats[2], // Last Month Tickets
                        currencyFormat.format(stats[3]) // Last Month Revenue
                });

                grandTotalCurrentTickets += stats[0];
                grandTotalCurrentRevenue += stats[1];
                grandTotalLastTickets += stats[2];
                grandTotalLastRevenue += stats[3];
            }

            // Add total row
            if (!employeeStats.isEmpty()) {
                tableModel.addRow(new Object[]{
                        "TỔNG",
                        "",
                        grandTotalCurrentTickets,
                        currencyFormat.format(grandTotalCurrentRevenue),
                        grandTotalLastTickets,
                        currencyFormat.format(grandTotalLastRevenue)
                });
            } else {
                JLabel noEmployeesLabel = new JLabel("Không tìm thấy nhân viên.");
                noEmployeesLabel.setForeground(LIGHT_TEXT_COLOR);
                noEmployeesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noEmployeesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                salesSummaryPanel.add(Box.createVerticalGlue());
                salesSummaryPanel.add(noEmployeesLabel);
                salesSummaryPanel.add(Box.createVerticalGlue());
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(salesSummaryPanel, "Lỗi khi tải dữ liệu giao dịch: " + e.getMessage(), "Lỗi Cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String getEmployeeName(String email) {
        // Placeholder logic to get employee name from email
        return email.split("@")[0]; // Example: nickphuso10505, test
        // Replace with actual logic to fetch name from database if needed
    }
}