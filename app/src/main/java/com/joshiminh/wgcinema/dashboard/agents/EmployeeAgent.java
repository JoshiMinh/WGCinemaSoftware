package com.joshiminh.wgcinema.dashboard.agents;

import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

public class EmployeeAgent extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JFormattedTextField dobField;
    private final JCheckBox adminCheckBox = new JCheckBox("Admin Privileges");
    private final JButton saveButton = new JButton("Save Changes");
    private final String dbUrl;
    private final String employeeEmail;
    private final Runnable refreshCallback;

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EmployeeAgent(String dbUrl, String employeeEmail, Runnable refreshCallback) {
        this.dbUrl = dbUrl;
        this.employeeEmail = employeeEmail;
        this.refreshCallback = refreshCallback;

        applyFrameDefaults(this, "Edit Employee: " + employeeEmail, 600, 600);
        setIconImage(ResourceUtil.loadAppIcon());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PRIMARY_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        gbc.gridwidth = 2;
        formPanel.add(createHeaderLabel("Edit Employee Details"), gbc);
        gbc.gridy++; gbc.gridwidth = 1;

        // Email (read-only)
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(TEXT_COLOR);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField.setText(employeeEmail);
        emailField.setEditable(false); // Email is the primary key, not editable
        emailField.setBorder(componentBorder());
        styleComponent(emailField);
        formPanel.add(emailField, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy++;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(TEXT_COLOR);
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        nameField.setBorder(componentBorder());
        styleComponent(nameField);
        formPanel.add(nameField, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy++;
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setForeground(TEXT_COLOR);
        formPanel.add(genderLabel, gbc);
        gbc.gridx = 1;
        styleComponent(genderBox);
        formPanel.add(genderBox, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy++;
        JLabel dobLabel = new JLabel("Date of Birth (DD/MM/YYYY):");
        dobLabel.setForeground(TEXT_COLOR);
        formPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        JFormattedTextField tempDobField;
        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            tempDobField = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            tempDobField = new JFormattedTextField();
        }
        dobField = tempDobField;
        dobField.setColumns(20);
        dobField.setBorder(componentBorder());
        styleComponent(dobField);
        formPanel.add(dobField, gbc);

        // Admin Checkbox
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        adminCheckBox.setForeground(TEXT_COLOR);
        adminCheckBox.setBackground(PRIMARY_BACKGROUND);
        adminCheckBox.setFont(LABEL_FONT);
        formPanel.add(adminCheckBox, gbc);

        // Save Button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        styleButton(saveButton);
        saveButton.addActionListener(this::handleSave);
        formPanel.add(saveButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        loadEmployeeDetails(); // Load existing data
    }

    private void loadEmployeeDetails() {
        try (ResultSet rs = DAO.fetchAccountByEmail(dbUrl, employeeEmail)) {
            if (rs != null && rs.next()) {
                nameField.setText(rs.getString("name"));
                genderBox.setSelectedItem(rs.getString("gender"));
                java.sql.Date dob = rs.getDate("date_of_birth");
                if (dob != null) {
                    dobField.setText(dob.toLocalDate().format(INPUT_FORMATTER));
                }
                adminCheckBox.setSelected(rs.getInt("admin") == 1);
            } else {
                JOptionPane.showMessageDialog(this, "Employee details not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error loading details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            dispose();
        }
    }

    private void handleSave(ActionEvent e) {
        String name = nameField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String dobStr = dobField.getText().trim();
        boolean isAdmin = adminCheckBox.isSelected();

        if (name.isEmpty() || dobStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, INPUT_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use DD/MM/YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Removed try-catch for SQLException here, as DAO handles it internally
        int updatedRows = DAO.updateAccount(dbUrl, employeeEmail, name, gender, java.sql.Date.valueOf(dob.format(DB_FORMATTER)), isAdmin);
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Employee details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (refreshCallback != null) {
                refreshCallback.run(); // Refresh the employee list
            }
            dispose();
        } else {
            // The DAO returns 0 if there's a database error or no rows were affected.
            // We can assume 0 means failure, and the DAO already prints stack trace.
            JOptionPane.showMessageDialog(this, "Failed to update employee details. No changes made or database error occurred.", "Update Failed", JOptionPane.WARNING_MESSAGE);
        }
    }
}
