package com.joshiminh.wgcinema.dashboard;

import java.awt.*;
import javax.swing.*;
import com.joshiminh.wgcinema.App;
import com.joshiminh.wgcinema.dashboard.sections.Movies;
import com.joshiminh.wgcinema.dashboard.sections.Showrooms;
import com.joshiminh.wgcinema.dashboard.sections.Showtimes;
import com.joshiminh.wgcinema.dashboard.sections.Employees;
import com.joshiminh.wgcinema.dashboard.sections.EmployeeSalesSummary; // Import the new EmployeeSalesSummary section
import com.joshiminh.wgcinema.utils.ResourceUtil;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

public class Dashboard extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private String databaseUrl;
    private JPanel sidebar;

    public Dashboard(String url) {
        this.databaseUrl = url;
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 1000);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PRIMARY_BACKGROUND);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(ResourceUtil.loadAppIcon());
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                new App();
            }
        });

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(PRIMARY_BACKGROUND);

        Movies moviesSection = new Movies(databaseUrl);
        Showrooms showroomsSection = new Showrooms(databaseUrl);
        Showtimes showtimesSection = new Showtimes(databaseUrl);
        Employees employeesSection = new Employees(databaseUrl);
        EmployeeSalesSummary employeeSalesSummarySection = new EmployeeSalesSummary(databaseUrl); // Initialize new EmployeeSalesSummary section

        mainContentPanel.add(moviesSection.getMoviesSection(), "Movies");
        mainContentPanel.add(showroomsSection.getShowroomsPanel(), "Showrooms");
        mainContentPanel.add(showtimesSection.getShowtimesPanel(), "Showtimes");
        mainContentPanel.add(employeesSection.getEmployeesPanel(), "Employees");
        mainContentPanel.add(employeeSalesSummarySection.getSalesSummaryPanel(), "Employee Sales Summary"); // Add Employee Sales Summary panel

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        cardLayout.show(mainContentPanel, "Movies");
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(SECONDARY_BACKGROUND);
        panel.setPreferredSize(new Dimension(220, getHeight()));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;

        JLabel logoLabel = new JLabel("WG Cinema Admin", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(ACCENT_BLUE);
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 30, 0);
        panel.add(logoLabel, gbc);

        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.gridy++;
        panel.add(createSidebarButton("Movies", "Movies"), gbc);
        gbc.gridy++;
        panel.add(createSidebarButton("Showrooms", "Showrooms"), gbc);
        gbc.gridy++;
        panel.add(createSidebarButton("Showtimes", "Showtimes"), gbc);
        gbc.gridy++;
        panel.add(createSidebarButton("Manage Employees", "Employees"), gbc); // Renamed button for clarity
        gbc.gridy++;
        panel.add(createSidebarButton("Employee Sales Summary", "Employee Sales Summary"), gbc); // New button for sales summary

        gbc.gridy++;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JButton createSidebarButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(LIGHT_TEXT_COLOR);
        button.setBackground(SECONDARY_BACKGROUND);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> cardLayout.show(mainContentPanel, panelName));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!button.getClientProperty("selected").equals(true)) {
                    button.setBackground(SECONDARY_BACKGROUND.brighter());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!button.getClientProperty("selected").equals(true)) {
                    button.setBackground(SECONDARY_BACKGROUND);
                }
            }
        });

        button.putClientProperty("selected", false);

        button.addActionListener(e -> {
            for (Component comp : sidebar.getComponents()) {
                if (comp instanceof JButton sb) {
                    sb.putClientProperty("selected", false);
                    sb.setBackground(SECONDARY_BACKGROUND);
                    sb.setForeground(LIGHT_TEXT_COLOR);
                }
            }
            button.putClientProperty("selected", true);
            button.setBackground(ACCENT_BLUE);
            button.setForeground(TEXT_COLOR);
        });

        if (panelName.equals("Movies")) { // Default selected panel
            button.putClientProperty("selected", true);
            button.setBackground(ACCENT_BLUE);
            button.setForeground(TEXT_COLOR);
        }

        return button;
    }
}
