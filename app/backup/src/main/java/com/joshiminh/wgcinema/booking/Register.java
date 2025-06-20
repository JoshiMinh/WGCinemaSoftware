package com.joshiminh.wgcinema.booking;

import com.joshiminh.wgcinema.utils.AgentStyles;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static com.joshiminh.wgcinema.utils.AgentStyles.*; // Import AgentStyles

public class Register extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JTextField nameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JFormattedTextField dobField;
    private final JButton registerButton = new JButton("Register");
    private final String dbUrl;

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Register(String dbUrl) {
        this.dbUrl = dbUrl;
        AgentStyles.applyFrameDefaults(this, "Register Account", 600, 600);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        formPanel.setBorder(BorderFactory.createEmptyBorder(
                FORM_PADDING, FORM_PADDING,
                FORM_PADDING, FORM_PADDING));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        gbc.gridwidth = 2;
        formPanel.add(AgentStyles.createHeaderLabel("Create Account"), gbc);
        gbc.gridy++; gbc.gridwidth = 1;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField.setBorder(componentBorder());
        AgentStyles.styleComponent(emailField);
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        nameField.setBorder(componentBorder());
        AgentStyles.styleComponent(nameField);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        formPanel.add(genderLabel, gbc);
        gbc.gridx = 1;
        AgentStyles.styleComponent(genderBox);
        formPanel.add(genderBox, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel dobLabel = new JLabel("Date of Birth (DD/MM/YYYY):");
        dobLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
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
        AgentStyles.styleComponent(dobField);
        formPanel.add(dobField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField.setBorder(componentBorder());
        AgentStyles.styleComponent(passwordField);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        confirmPasswordField.setBorder(componentBorder());
        AgentStyles.styleComponent(confirmPasswordField);
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        AgentStyles.styleButton(registerButton);
        registerButton.addActionListener(this::handleRegister);
        formPanel.add(registerButton, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void handleRegister(ActionEvent e) {
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String dobStr = dobField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (email.isEmpty() || name.isEmpty() || dobStr.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, INPUT_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use DD/MM/YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO accounts (account_email, name, gender, date_of_birth, membership, transactions, password_hash, admin) VALUES (?, ?, ?, ?, 0, 0, ?, 0)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, name);
            stmt.setString(3, gender);
            stmt.setDate(4, Date.valueOf(dob.format(DB_FORMATTER)));
            stmt.setString(5, hashPassword(password));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Email already registered.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
