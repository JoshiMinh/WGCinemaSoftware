package com.joshiminh.wgcinema.booking;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.joshiminh.wgcinema.App;
import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.*;
import static com.joshiminh.wgcinema.utils.AgentStyles.*; // Import AgentStyles

@SuppressWarnings("deprecation")
public class MovieList extends JFrame {
    private static final Color BACKGROUND_COLOR = PRIMARY_BACKGROUND; // Use PRIMARY_BACKGROUND
    private static final Color CARD_COLOR = SECONDARY_BACKGROUND; // Use SECONDARY_BACKGROUND
    private static final Color BUTTON_COLOR = ACCENT_TEAL; // Use ACCENT_TEAL
    private static final Color TOP_BAR_COLOR = PRIMARY_BACKGROUND; // Use PRIMARY_BACKGROUND
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final int CARD_WIDTH = 260;
    private static final int CARD_HEIGHT = 520;
    private static final int CARDS_PER_ROW = 4;
    private static final int CARD_RADIUS = 24;

    private final String url;
    private String email;
    private JPanel moviePanel;
    private JTextField searchField; // New: Search input field
    private JButton searchButton;   // New: Search button
    private JButton showAllButton;  // New: Show All button

    public MovieList(String url, String email) {
        this.url = url;
        this.email = email;
        initializeFrame();
        setupComponents();
        loadInitialMovieList(); // Call initial load
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("Now Showing");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(ResourceUtil.loadAppIcon());
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                new App();
            }
        });
    }

    private void setupComponents() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createMovieScrollPane(), BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(20, 0));
        topBar.setBackground(TOP_BAR_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JButton transactionHistoryButton = new JButton("Transaction History");
        transactionHistoryButton.setBackground(ACCENT_BLUE.darker()); // Use ACCENT_BLUE.darker()
        transactionHistoryButton.setPreferredSize(new Dimension(220, 48));
        transactionHistoryButton.setMinimumSize(new Dimension(220, 48));
        transactionHistoryButton.setMaximumSize(new Dimension(220, 48));
        transactionHistoryButton.setBorderPainted(false);
        transactionHistoryButton.setFocusPainted(false);
        transactionHistoryButton.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        transactionHistoryButton.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
        transactionHistoryButton.setHorizontalAlignment(SwingConstants.CENTER);
        transactionHistoryButton.setHorizontalTextPosition(SwingConstants.CENTER);
        transactionHistoryButton.addActionListener(_ -> new TransactionHistory(url, email).setVisible(true));
        transactionHistoryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                transactionHistoryButton.setBackground(ACCENT_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                transactionHistoryButton.setBackground(ACCENT_BLUE.darker());
            }
        });

        JLabel logo = new JLabel();
        Image image = ResourceUtil.loadImage("/images/icon.png").getScaledInstance(60, 54, Image.SCALE_SMOOTH);
        logo.setIcon(new ImageIcon(image));

        // New: Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setBackground(TOP_BAR_COLOR);

        searchField = new JTextField(30); // Adjust size
        searchField.setFont(BASE_FONT.deriveFont(Font.PLAIN, 16f));
        searchField.setForeground(TEXT_COLOR);
        searchField.setBackground(SECONDARY_BACKGROUND);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setCaretColor(TEXT_COLOR); // Set caret color

        searchButton = new JButton("Search");
        AgentStyles.styleButton(searchButton); // Apply consistent style
        searchButton.addActionListener(_ -> performSearch());

        showAllButton = new JButton("Show All");
        AgentStyles.styleButton(showAllButton);
        showAllButton.addActionListener(_ -> {
            searchField.setText(""); // Clear search field
            performSearch(); // Show all movies
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        topBar.add(transactionHistoryButton, BorderLayout.EAST);
        topBar.add(logo, BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.CENTER); // Add search panel to the center
        return topBar;
    }

    private JScrollPane createMovieScrollPane() {
        moviePanel = new JPanel(new GridBagLayout());
        moviePanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(moviePanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        return scrollPane;
    }

    private void loadInitialMovieList() {
        performSearch(); // Call performSearch with empty query initially to load all movies
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        moviePanel.removeAll(); // Clear existing movie cards
        moviePanel.revalidate();
        moviePanel.repaint();

        ResultSet resultSet = null;
        if (query.isEmpty()) {
            resultSet = DAO.fetchUpcomingMovies(url);
        } else {
            resultSet = DAO.searchMoviesByTitle(url, query);
        }

        if (resultSet == null) {
            JLabel errorLabel = new JLabel("Error loading movies or database connection issue.", SwingConstants.CENTER);
            errorLabel.setForeground(ERROR_COLOR);
            errorLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            moviePanel.add(errorLabel, new GridBagConstraints());
            moviePanel.revalidate();
            moviePanel.repaint();
            return;
        }

        try {
            if (!resultSet.isBeforeFirst()) { // Check if ResultSet is empty
                JLabel noResultsLabel = new JLabel("No movies found matching your search.", SwingConstants.CENTER);
                noResultsLabel.setForeground(TEXT_COLOR);
                noResultsLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
                moviePanel.add(noResultsLabel, new GridBagConstraints());
            } else {
                displayMovies(resultSet);
            }
            resultSet.getStatement().getConnection().close();
        } catch (SQLException | java.net.MalformedURLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error displaying movies: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(ERROR_COLOR);
            errorLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            moviePanel.add(errorLabel, new GridBagConstraints());
        } finally {
            moviePanel.revalidate();
            moviePanel.repaint();
        }
    }

    private void displayMovies(ResultSet resultSet) throws SQLException, java.net.MalformedURLException {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(24, 24, 24, 24);
        gbc.gridy = 0;
        gbc.gridx = 0;

        int col = 0;
        while (resultSet.next()) {
            JPanel movie = createMovieCard(
                resultSet.getInt("id"),
                resultSet.getString("poster"),
                resultSet.getString("title"),
                resultSet.getString("age_rating"),
                resultSet.getString("director"),
                resultSet.getInt("duration")
            );
            gbc.gridx = col++;
            moviePanel.add(movie, gbc);
            
            if (col == CARDS_PER_ROW) {
                col = 0;
                gbc.gridy++;
            }
        }
    }

    private JPanel createMovieCard(int movieId, String imageLink, String titleText, String rating, String director, int duration) 
            throws java.net.MalformedURLException {
        JPanel card = new RoundedPanel(CARD_RADIUS, CARD_COLOR);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        card.add(createPosterLabel(imageLink), BorderLayout.NORTH);
        card.add(createInfoPanel(titleText, rating, director, duration), BorderLayout.CENTER);
        
        JButton bookButton = createBookButton(movieId);
        card.add(bookButton, BorderLayout.SOUTH);

        // Add MouseListener to the card to open MovieDetailView, but exclude the bookButton
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the click was not on the bookButton
                if (e.getSource() == card && e.getComponent() != bookButton && !SwingUtilities.isDescendingFrom(e.getComponent(), bookButton)) {
                    new MovieDetailView(movieId, url);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(Cursor.getDefaultCursor());
            }
        });

        return card;
    }

    private JLabel createPosterLabel(String imageLink) throws java.net.MalformedURLException {
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon icon = new ImageIcon(new URL(imageLink));
        Image scaled = icon.getImage().getScaledInstance(CARD_WIDTH, (CARD_WIDTH * 3) / 2, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        return imageLabel;
    }

    private JPanel createInfoPanel(String titleText, String rating, String director, int duration) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        MultiLineLabel titleLabel = createTitleLabel(titleText);
        JLabel directorLabel = createDetailLabel("Director: " + director);
        JLabel durationLabel = createDetailLabel("Duration: " + duration + " min");
        JLabel ratingLabel = createRatingLabel(rating);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(directorLabel);
        infoPanel.add(durationLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(ratingLabel);
        return infoPanel;
    }

    private MultiLineLabel createTitleLabel(String titleText) {
        MultiLineLabel titleLabel = new MultiLineLabel(titleText, BASE_FONT, 2);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        titleLabel.setMaximumSize(new Dimension(CARD_WIDTH - 32, Integer.MAX_VALUE));
        return titleLabel;
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(LIGHT_TEXT_COLOR);
        label.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return label;
    }

    private JLabel createRatingLabel(String rating) {
        JLabel ratingLabel = new JLabel(rating, SwingConstants.CENTER);
        ratingLabel.setForeground(AgeRatingColor.getColorForRating(rating));
        ratingLabel.setFont(BASE_FONT.deriveFont(Font.PLAIN, 16f));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ratingLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return ratingLabel;
    }

    private JButton createBookButton(int movieId) {
        JButton bookButton = new JButton("Book Ticket");
        bookButton.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
        bookButton.setForeground(TEXT_COLOR);
        bookButton.setBackground(BUTTON_COLOR);
        bookButton.setFocusPainted(false);
        bookButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookButton.addActionListener(_ -> new Booking(movieId, url).setVisible(true));
        bookButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bookButton.setBackground(BUTTON_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bookButton.setBackground(BUTTON_COLOR);
            }
        });
        return bookButton;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class MultiLineLabel extends JLabel {
        private final int maxLines;

        public MultiLineLabel(String text, Font font, int maxLines) {
            super(text);
            setFont(font);
            this.maxLines = maxLines;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            
            List<String> lines = calculateLines(fm);
            drawLines(g2, fm, lines);
            g2.dispose();
        }

        private List<String> calculateLines(FontMetrics fm) {
            int width = getWidth();
            String[] words = getText().split("\\s+");
            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();

            for (String word : words) {
                StringBuilder trial = new StringBuilder(current).append(word).append(" ");
                if (fm.stringWidth(trial.toString()) > width) {
                    lines.add(current.toString());
                    current = new StringBuilder(word).append(" ");
                } else {
                    current = trial;
                }
                if (lines.size() == maxLines) break;
            }

            if (lines.size() < maxLines && current.length() > 0) {
                lines.add(current.toString());
            }

            return finalizeLines(lines, fm);
        }

        private List<String> finalizeLines(List<String> lines, FontMetrics fm) {
            if (lines.size() > maxLines) {
                lines = lines.subList(0, maxLines);
            }
            if (lines.size() == maxLines) {
                String last = lines.get(maxLines - 1).trim();
                while (fm.stringWidth(last + "...") > getWidth() && last.length() > 0) {
                    last = last.substring(0, last.length() - 1);
                }
                lines.set(maxLines - 1, last + "...");
            }
            return lines;
        }

        private void drawLines(Graphics2D g2, FontMetrics fm, List<String> lines) {
            int lineHeight = fm.getHeight();
            int y = fm.getAscent();
            int width = getWidth();

            for (String line : lines) {
                String text = line.trim();
                int textWidth = fm.stringWidth(text);
                int x = (width - textWidth) / 2;
                g2.drawString(text, x, y);
                y += lineHeight;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(super.getPreferredSize().width, fm.getHeight() * maxLines);
        }
    }
}