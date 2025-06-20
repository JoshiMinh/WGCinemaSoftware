package com.joshiminh.wgcinema.booking;

import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import static com.joshiminh.wgcinema.utils.AgentStyles.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieDetailView extends JFrame {

    private final String url;
    private final int movieId;
    private String movieTitle; // To pass to Booking frame

    public MovieDetailView(int movieId, String url) {
        this.movieId = movieId;
        this.url = url;
        initializeFrame();
        loadMovieDetails();
    }

    private void initializeFrame() {
        setTitle("Movie Details");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700); // Adjusted size for more content
        setLocationRelativeTo(null);
        setIconImage(ResourceUtil.loadAppIcon());
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY_BACKGROUND);
    }

    private void loadMovieDetails() {
        ResultSet rs = DAO.fetchMovieDetails(url, movieId);
        if (rs == null) {
            JOptionPane.showMessageDialog(this, "Failed to load movie details.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        try {
            if (rs.next()) {
                String posterUrl = rs.getString("poster");
                movieTitle = rs.getString("title");
                String director = rs.getString("director");
                int duration = rs.getInt("duration");
                String ageRating = rs.getString("age_rating");
                String description = rs.getString("description");
                String trailerUrl = rs.getString("trailer"); // Changed from "trailer_url" to "trailer"

                displayMovieDetails(posterUrl, movieTitle, director, duration, ageRating, description, trailerUrl);
            } else {
                JOptionPane.showMessageDialog(this, "Movie not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
            rs.getStatement().getConnection().close();
        } catch (SQLException | java.net.MalformedURLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing movie data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void displayMovieDetails(String posterUrl, String title, String director, int duration, String ageRating, String description, String trailerUrl) throws java.net.MalformedURLException {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(PRIMARY_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left Panel for Poster
        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon icon = new ImageIcon(new URL(posterUrl));
        Image scaled = icon.getImage().getScaledInstance(300, 450, Image.SCALE_SMOOTH); // Larger poster
        posterLabel.setIcon(new ImageIcon(scaled));
        posterLabel.setBorder(BorderFactory.createLineBorder(ACCENT_TEAL, 2)); // Add a border

        JPanel posterPanel = new JPanel(new BorderLayout());
        posterPanel.setBackground(PRIMARY_BACKGROUND);
        posterPanel.add(posterLabel, BorderLayout.CENTER);

        // Right Panel for Details and Description
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(PRIMARY_BACKGROUND);
        detailsPanel.setBorder(new EmptyBorder(0, 10, 0, 0)); // Padding from poster

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel directorLabel = new JLabel("Director: " + director);
        directorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        directorLabel.setForeground(LIGHT_TEXT_COLOR);
        directorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel durationLabel = new JLabel("Duration: " + duration + " min");
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        durationLabel.setForeground(LIGHT_TEXT_COLOR);
        durationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ageRatingLabel = new JLabel("Age Rating: " + ageRating);
        ageRatingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        ageRatingLabel.setForeground(AgeRatingColor.getColorForRating(ageRating));
        ageRatingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ageRatingLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Spacing

        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionArea.setForeground(TEXT_COLOR);
        descriptionArea.setBackground(SECONDARY_BACKGROUND); // Darker background for text area
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descriptionScrollPane.setPreferredSize(new Dimension(400, 150)); // Set preferred size
        descriptionScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionScrollPane.setBorder(null); // Remove default scroll pane border

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(directorLabel);
        detailsPanel.add(durationLabel);
        detailsPanel.add(ageRatingLabel);
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(descriptionScrollPane);
        detailsPanel.add(Box.createVerticalGlue()); // Pushes content to top

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(PRIMARY_BACKGROUND);

        JButton watchTrailerButton = new JButton("Watch Trailer");
        styleButton(watchTrailerButton);
        watchTrailerButton.setBackground(ACCENT_BLUE); // Different color for trailer button
        watchTrailerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                watchTrailerButton.setBackground(ACCENT_BLUE.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                watchTrailerButton.setBackground(ACCENT_BLUE);
            }
        });
        watchTrailerButton.addActionListener(e -> {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                try {
                    Desktop.getDesktop().browse(new URI(trailerUrl));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not open trailer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "No trailer URL available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton bookTicketsButton = new JButton("Book Tickets");
        styleButton(bookTicketsButton);
        bookTicketsButton.setBackground(ACCENT_TEAL); // Use ACCENT_TEAL for booking
        bookTicketsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bookTicketsButton.setBackground(ACCENT_TEAL.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bookTicketsButton.setBackground(ACCENT_TEAL);
            }
        });
        bookTicketsButton.addActionListener(e -> {
            new Booking(movieId, url).setVisible(true);
            dispose(); // Close the detail view
        });

        buttonPanel.add(watchTrailerButton);
        buttonPanel.add(bookTicketsButton);

        mainPanel.add(posterPanel, BorderLayout.WEST);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}
