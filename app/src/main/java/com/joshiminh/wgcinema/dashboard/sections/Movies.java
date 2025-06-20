package com.joshiminh.wgcinema.dashboard.sections;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.joshiminh.wgcinema.dashboard.agents.MovieAgent;
import com.joshiminh.wgcinema.dashboard.agents.MovieSearch;
import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.*;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

@SuppressWarnings("unused")
public class Movies {
    private String url;
    private JPanel moviesSection;
    private JPanel moviesPanel;
    private JPanel currentChartPanel;

    public Movies(String url) {
        this.url = url;
        this.moviesSection = new JPanel(new BorderLayout());
        this.moviesSection.setBackground(PRIMARY_BACKGROUND);
        this.moviesSection.add(createSearchBarPanel(), BorderLayout.NORTH);
        this.moviesPanel = new JPanel();
        this.moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.Y_AXIS));
        this.moviesPanel.setBackground(PRIMARY_BACKGROUND);
        loadMovies();
        JScrollPane scrollPane = new JScrollPane(moviesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(PRIMARY_BACKGROUND);
        this.moviesSection.add(scrollPane, BorderLayout.CENTER);
        this.moviesSection.add(createChartPanel(), BorderLayout.SOUTH);
    }

    public JPanel getMoviesSection() {
        return moviesSection;
    }

    private void loadMovies() {
        moviesPanel.removeAll();
        try (ResultSet resultSet = DAO.fetchAllMovies(url)) {
            while (resultSet != null && resultSet.next()) {
                moviesPanel.add(createMovieEntryPanel(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("age_rating"),
                        resultSet.getString("release_date")
                ));
                moviesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading movies: " + e.getMessage());
            errorLabel.setForeground(DANGER_RED);
            moviesPanel.add(errorLabel);
        }
        moviesPanel.revalidate();
        moviesPanel.repaint();
        updateChartPanel();
    }

    private JPanel createMovieEntryPanel(int id, String title, String ageRating, String releaseDate) {
        JPanel movieEntryPanel = new JPanel(new BorderLayout(10, 0));
        movieEntryPanel.setBackground(SECONDARY_BACKGROUND);
        movieEntryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        movieEntryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(SECONDARY_BACKGROUND);
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ageRatingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ageRatingPanel.setBackground(SECONDARY_BACKGROUND);
        ageRatingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ageRatingTextLabel = new JLabel("Age Rating: ");
        ageRatingTextLabel.setForeground(LIGHT_TEXT_COLOR);
        ageRatingTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ageRatingPanel.add(ageRatingTextLabel);

        JLabel ageRatingValueLabel = new JLabel(ageRating);
        ageRatingValueLabel.setForeground(AgeRatingColor.getColorForRating(ageRating));
        ageRatingValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ageRatingPanel.add(ageRatingValueLabel);

        JLabel releaseDateLabel = new JLabel("Release Date: " + releaseDate);
        releaseDateLabel.setForeground(LIGHT_TEXT_COLOR);
        releaseDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        releaseDateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(ageRatingPanel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(releaseDateLabel);
        movieEntryPanel.add(textPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(SECONDARY_BACKGROUND);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(DANGER_RED);
        deleteButton.setForeground(TEXT_COLOR);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteButton.addActionListener(e -> deleteMovie(id));
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(DANGER_RED_BRIGHTER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(DANGER_RED);
            }
        });

        JButton editButton = new JButton("Edit");
        editButton.setBackground(ACCENT_TEAL);
        editButton.setForeground(TEXT_COLOR);
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.addActionListener(e -> new MovieAgent(url, id, false, () -> loadMovies()).setVisible(true));
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                editButton.setBackground(ACCENT_TEAL.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                editButton.setBackground(ACCENT_TEAL);
            }
        });

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        movieEntryPanel.add(buttonPanel, BorderLayout.EAST);
        return movieEntryPanel;
    }

    private void deleteMovie(int id) {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this movie?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int result = DAO.deleteRowById(url, "movies", "id", id);
            if (result > 0) {
                loadMovies();
            } else {
                JOptionPane.showMessageDialog(null, "Error deleting movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createSearchBarPanel() {
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        searchBarPanel.setBackground(SECONDARY_BACKGROUND);
        searchBarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton newMovieButton = new JButton("New Movie");
        newMovieButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newMovieButton.setBackground(ACCENT_BLUE);
        newMovieButton.setForeground(TEXT_COLOR);
        newMovieButton.setFocusPainted(false);
        newMovieButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        newMovieButton.addActionListener(e -> new MovieAgent(url, -1, true, () -> loadMovies()).setVisible(true));
        newMovieButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                newMovieButton.setBackground(ACCENT_BLUE.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                newMovieButton.setBackground(ACCENT_BLUE);
            }
        });
        searchBarPanel.add(newMovieButton);

        JTextField searchBar = new JTextField(25);
        styleComponent(searchBar);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchBar.setCaretColor(TEXT_COLOR);
        searchBarPanel.add(searchBar);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(ACCENT_TEAL);
        searchButton.setForeground(TEXT_COLOR);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchButton.addActionListener(e -> {
            String query = searchBar.getText().trim();
            if (!query.isEmpty()) {
                new MovieSearch(url, query).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a search query.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                searchButton.setBackground(ACCENT_TEAL.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                searchButton.setBackground(ACCENT_TEAL);
            }
        });
        searchBar.addActionListener(e -> searchButton.doClick());
        searchBarPanel.add(searchButton);
        return searchBarPanel;
    }

    private JPanel createChartPanel() {
        currentChartPanel = new JPanel(new GridLayout(1, 3));
        currentChartPanel.setBackground(PRIMARY_BACKGROUND);
        updateChartPanel();
        return currentChartPanel;
    }

    private void updateChartPanel() {
        if (currentChartPanel == null) return;

        currentChartPanel.removeAll();

        DefaultPieDataset ageRatingDataset = new DefaultPieDataset();
        try (ResultSet resultSet = DAO.fetchAgeRatingCounts(url)) {
            while (resultSet != null && resultSet.next()) {
                ageRatingDataset.setValue(resultSet.getString("age_rating"), resultSet.getInt("count"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading age rating pie chart data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart ageRatingChart = ChartFactory.createPieChart("Movies by Age Rating", ageRatingDataset, true, true, false);
        ageRatingChart.getTitle().setPaint(TEXT_COLOR);
        PiePlot agePlot = (PiePlot) ageRatingChart.getPlot();
        agePlot.setBackgroundPaint(PRIMARY_BACKGROUND);
        agePlot.setOutlinePaint(BORDER_COLOR);
        agePlot.setLabelBackgroundPaint(SECONDARY_BACKGROUND);
        agePlot.setLabelPaint(TEXT_COLOR);
        agePlot.setSectionPaint("PG", AgeRatingColor.getColorForRating("PG"));
        agePlot.setSectionPaint("PG-13", AgeRatingColor.getColorForRating("PG-13"));
        agePlot.setSectionPaint("PG-16", AgeRatingColor.getColorForRating("PG-16"));
        agePlot.setSectionPaint("R", AgeRatingColor.getColorForRating("R"));
        ageRatingChart.setBackgroundPaint(PRIMARY_BACKGROUND);

        ChartPanel ageChartPanel = new ChartPanel(ageRatingChart);
        ageChartPanel.setPreferredSize(new Dimension(300, 300));
        ageChartPanel.setBackground(PRIMARY_BACKGROUND);
        ageChartPanel.setMouseWheelEnabled(true);
        ageChartPanel.setDomainZoomable(true);
        ageChartPanel.setRangeZoomable(true);
        currentChartPanel.add(ageChartPanel);

        DefaultPieDataset languageDataset = new DefaultPieDataset();
        int totalMovies = 0;
        try (ResultSet resultSet = DAO.fetchLanguageCounts(url)) {
            while (resultSet != null && resultSet.next()) {
                totalMovies += resultSet.getInt("count");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading language pie chart data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        try (ResultSet resultSet = DAO.fetchLanguageCounts(url)) {
            while (resultSet != null && resultSet.next()) {
                String language = resultSet.getString("language");
                double percentage = (resultSet.getInt("count") * 100.0) / totalMovies;
                languageDataset.setValue(language, percentage);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error calculating language percentages: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart languageChart = ChartFactory.createPieChart("Movies by Language", languageDataset, true, true, false);
        languageChart.getTitle().setPaint(TEXT_COLOR);
        PiePlot languagePlot = (PiePlot) languageChart.getPlot();
        languagePlot.setBackgroundPaint(PRIMARY_BACKGROUND);
        languagePlot.setOutlinePaint(BORDER_COLOR);
        languagePlot.setLabelBackgroundPaint(SECONDARY_BACKGROUND);
        languagePlot.setLabelPaint(TEXT_COLOR);
        languagePlot.setSectionPaint("English", ACCENT_BLUE);
        languagePlot.setSectionPaint("Vietnamese", ACCENT_TEAL);
        languagePlot.setSectionPaint("Other", LIGHT_TEXT_COLOR);
        languageChart.setBackgroundPaint(PRIMARY_BACKGROUND);

        ChartPanel languageChartPanel = new ChartPanel(languageChart);
        languageChartPanel.setPreferredSize(new Dimension(300, 300));
        languageChartPanel.setBackground(PRIMARY_BACKGROUND);
        languageChartPanel.setMouseWheelEnabled(true);
        languageChartPanel.setDomainZoomable(true);
        languageChartPanel.setRangeZoomable(true);
        currentChartPanel.add(languageChartPanel);

        currentChartPanel.revalidate();
        currentChartPanel.repaint();
    }
}
