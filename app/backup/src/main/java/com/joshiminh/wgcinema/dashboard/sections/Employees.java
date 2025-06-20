package com.joshiminh.wgcinema.dashboard.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

import com.joshiminh.wgcinema.dashboard.agents.EmployeeAgent; // Import the new EmployeeAgent
import com.joshiminh.wgcinema.data.DAO;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;
import com.joshiminh.wgcinema.utils.TableStyles; // Keep if needed for other tables, but not for this section anymore

public class Employees {
    private String url;
    private JPanel employeesSectionPanel; // Renamed to avoid confusion with inner panel
    private JPanel employeeListPanel; // This will hold the list of employee cards
    private Runnable refreshCallback; // To refresh the list after changes

    public Employees(String url) {
        this.url = url;
        this.employeesSectionPanel = new JPanel(new BorderLayout(15, 15));
        this.employeesSectionPanel.setBackground(PRIMARY_BACKGROUND);
        this.employeesSectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = createHeaderLabel("Manage Employees"); // Changed header
        employeesSectionPanel.add(headerLabel, BorderLayout.NORTH);

        employeeListPanel = new JPanel();
        employeeListPanel.setLayout(new BoxLayout(employeeListPanel, BoxLayout.Y_AXIS));
        employeeListPanel.setBackground(PRIMARY_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(employeeListPanel);
        scrollPane.getViewport().setBackground(PRIMARY_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        employeesSectionPanel.add(scrollPane, BorderLayout.CENTER);

        // Define refresh callback
        this.refreshCallback = () -> loadEmployeeManagementList();

        loadEmployeeManagementList(); // Initial load
    }

    public JPanel getEmployeesPanel() {
        return employeesSectionPanel;
    }

    private void loadEmployeeManagementList() {
        employeeListPanel.removeAll();
        boolean foundEmployees = false;
        try (ResultSet employeesRs = DAO.fetchEmployees(url)) { // DAO.fetchEmployees should return email and name
            if (employeesRs != null) {
                while (employeesRs.next()) {
                    foundEmployees = true;
                    String email = employeesRs.getString("account_email");
                    String name = employeesRs.getString("name");
                    employeeListPanel.add(createEmployeeEntryPanel(email, name));
                    employeeListPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing between entries
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(employeesSectionPanel, "Error loading employee data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (!foundEmployees) {
            JLabel noEmployeesLabel = new JLabel("No employees found.");
            noEmployeesLabel.setForeground(LIGHT_TEXT_COLOR);
            noEmployeesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            noEmployeesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            employeeListPanel.add(Box.createVerticalGlue());
            employeeListPanel.add(noEmployeesLabel);
            employeeListPanel.add(Box.createVerticalGlue());
        }

        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

    private JPanel createEmployeeEntryPanel(String email, String name) {
        JPanel entryPanel = new JPanel(new BorderLayout(15, 0));
        entryPanel.setBackground(SECONDARY_BACKGROUND);
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Fixed height for entries

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(SECONDARY_BACKGROUND);

        JLabel nameLabel = new JLabel("Name: " + name);
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = new JLabel("Email: " + email);
        emailLabel.setForeground(LIGHT_TEXT_COLOR);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(emailLabel);

        entryPanel.add(textPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(SECONDARY_BACKGROUND);

        JButton editButton = new JButton("Edit");
        styleButton(editButton); // Use existing styleButton
        editButton.setBackground(ACCENT_TEAL);
        editButton.addActionListener(e -> new EmployeeAgent(url, email, refreshCallback).setVisible(true));
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { editButton.setBackground(ACCENT_TEAL.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { editButton.setBackground(ACCENT_TEAL); }
        });

        JButton deleteButton = new JButton("Delete");
        styleButton(deleteButton); // Use existing styleButton
        deleteButton.setBackground(DANGER_RED);
        deleteButton.addActionListener(e -> deleteEmployee(email));
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { deleteButton.setBackground(DANGER_RED_BRIGHTER); }
            public void mouseExited(java.awt.event.MouseEvent e) { deleteButton.setBackground(DANGER_RED); }
        });

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        entryPanel.add(buttonPanel, BorderLayout.EAST);

        return entryPanel;
    }

    private void deleteEmployee(String email) {
        int confirm = JOptionPane.showConfirmDialog(
                employeesSectionPanel,
                "Are you sure you want to delete employee: " + email + "?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Use account_email as primary key for deletion
            int result = DAO.deleteRowById(url, "accounts", "account_email", email);
            if (result > 0) {
                JOptionPane.showMessageDialog(employeesSectionPanel, "Employee deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeManagementList(); // Refresh the list
            } else {
                JOptionPane.showMessageDialog(employeesSectionPanel, "Error deleting employee. Please check if there are associated records (e.g., transactions).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
