package com.joshiminh.wgcinema.dashboard.agents;

import java.awt.*;
import java.sql.*;
import javax.swing.*;

import com.joshiminh.wgcinema.dashboard.Dashboard;
import com.joshiminh.wgcinema.data.*;
import com.joshiminh.wgcinema.utils.*;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

@SuppressWarnings("unused")
public class MovieSearch extends JFrame {
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private static final int MAX_CARD_WIDTH = 480;
    private static final int CARD_HEIGHT = 140;

    private JPanel listPanel;
    private String currentQuery;
    private String databaseUrl;

    public MovieSearch(String url, String query) {
        this.databaseUrl = url;
        this.currentQuery = query;
        setTitle("Search: " + query);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 650);
        setLocationRelativeTo(null);
        setIconImage(ResourceUtil.loadAppIcon());

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(PRIMARY_BACKGROUND);
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refreshMoviesPanel();

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setContentPane(scrollPane);
    }

    private void refreshMoviesPanel() {
        listPanel.removeAll();
        boolean found = false;
        try (ResultSet resultSet = DAO.searchMoviesByTitle(databaseUrl, currentQuery)) {
            while (resultSet.next()) {
                found = true;
                listPanel.add(createMovieEntryPanel(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("age_rating"),
                        resultSet.getString("release_date"),
                        databaseUrl));
                listPanel.add(Box.createRigidArea(new Dimension(0, 14)));
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading movies: " + e.getMessage());
            errorLabel.setForeground(DANGER_RED);
            errorLabel.setFont(TITLE_FONT);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(errorLabel);
        }
        if (!found) {
            JLabel noResult = new JLabel("No movies found.");
            noResult.setForeground(LIGHT_TEXT_COLOR);
            noResult.setFont(TITLE_FONT);
            noResult.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(noResult);
            listPanel.add(Box.createVerticalGlue());
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createMovieEntryPanel(int id, String title, String ageRating, String releaseDate, String url) {
        JPanel movieEntryPanel = new JPanel(new BorderLayout(12, 0));
        movieEntryPanel.setBackground(SECONDARY_BACKGROUND);
        movieEntryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        movieEntryPanel.setPreferredSize(new Dimension(MAX_CARD_WIDTH, CARD_HEIGHT));
        movieEntryPanel.setMaximumSize(new Dimension(MAX_CARD_WIDTH, CARD_HEIGHT));
        movieEntryPanel.setMinimumSize(new Dimension(MAX_CARD_WIDTH, CARD_HEIGHT));

        movieEntryPanel.add(createTextPanel(title, ageRating, releaseDate), BorderLayout.CENTER);
        movieEntryPanel.add(createButtonPanel(id, url), BorderLayout.EAST);
        return movieEntryPanel;
    }

    private JPanel createTextPanel(String title, String ageRating, String releaseDate) {
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(SECONDARY_BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ageRatingPanel = createAgeRatingPanel(ageRating);

        JLabel releaseDateLabel = new JLabel("Release Date: " + releaseDate);
        releaseDateLabel.setForeground(LIGHT_TEXT_COLOR);
        releaseDateLabel.setFont(LABEL_FONT);
        releaseDateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(ageRatingPanel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 7)));
        textPanel.add(releaseDateLabel);
        return textPanel;
    }

    private JPanel createAgeRatingPanel(String ageRating) {
        JLabel ageRatingLabel = new JLabel("Age Rating: ");
        ageRatingLabel.setForeground(LIGHT_TEXT_COLOR);
        ageRatingLabel.setFont(LABEL_FONT);

        JLabel ageRatingValueLabel = new JLabel(ageRating);
        ageRatingValueLabel.setForeground(AgeRatingColor.getColorForRating(ageRating));
        ageRatingValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel ageRatingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ageRatingPanel.setBackground(SECONDARY_BACKGROUND);
        ageRatingPanel.add(ageRatingLabel);
        ageRatingPanel.add(ageRatingValueLabel);
        ageRatingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ageRatingPanel;
    }

    private JPanel createButtonPanel(int id, String url) {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(SECONDARY_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JButton editButton = createEditButton(id, url);
        JButton deleteButton = createDeleteButton(id, url);

        gbc.gridy = 0;
        buttonPanel.add(editButton, gbc);

        gbc.gridy = 1;
        buttonPanel.add(deleteButton, gbc);

        return buttonPanel;
    }

    private JButton createDeleteButton(int id, String url) {
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(DANGER_RED);
        deleteButton.setForeground(TEXT_COLOR);
        deleteButton.setFont(LABEL_FONT);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        deleteButton.setPreferredSize(new Dimension(90, 35));
        deleteButton.setMinimumSize(new Dimension(90, 35));
        deleteButton.setMaximumSize(new Dimension(90, 35));
        deleteButton.addActionListener(e -> {
            int rowsAffected = DAO.deleteRowById(url, "movies", "id", id);
            if (rowsAffected > 0) {
                refreshMoviesPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(DANGER_RED_BRIGHTER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(DANGER_RED);
            }
        });
        return deleteButton;
    }

    private JButton createEditButton(int id, String url) {
        JButton editButton = new JButton("Edit");
        editButton.setBackground(ACCENT_TEAL);
        editButton.setForeground(TEXT_COLOR);
        editButton.setFont(LABEL_FONT);
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        editButton.setPreferredSize(new Dimension(90, 35));
        editButton.setMinimumSize(new Dimension(90, 35));
        editButton.setMaximumSize(new Dimension(90, 35));
        editButton.addActionListener(e -> new MovieAgent(url, id, false, () -> refreshMoviesPanel()).setVisible(true));
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                editButton.setBackground(ACCENT_TEAL.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                editButton.setBackground(ACCENT_TEAL);
            }
        });
        return editButton;
    }
}
