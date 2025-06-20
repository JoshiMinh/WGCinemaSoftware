package com.joshiminh.wgcinema.booking;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.text.NumberFormat; // Import NumberFormat
import java.util.Locale; // Import Locale

import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import static com.joshiminh.wgcinema.utils.AgentStyles.*; // Import AgentStyles

public class TransactionHistory extends JFrame {
    private static final Color BACKGROUND_COLOR = PRIMARY_BACKGROUND; // Use PRIMARY_BACKGROUND

    public TransactionHistory(String url, String email) {
        setTitle("Your Ticket History");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setIconImage(ResourceUtil.loadAppIcon());

        JPanel transactionsPanel = createTransactionsPanel(url, email);
        transactionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(transactionsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setPreferredSize(new Dimension(470, 570));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        setContentPane(scrollPane);
    }

    private JPanel createTransactionsPanel(String url, String email) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        try (ResultSet rs = DAO.fetchTransactionHistory(url, email)) {
            boolean hasRows = false;
            while (rs != null && rs.next()) {
                hasRows = true;
                int movieId = rs.getInt("movie_id");
                try (ResultSet movieRs = DAO.fetchMovieDetails(url, movieId)) {
                    if (movieRs != null && movieRs.next()) {
                        panel.add(createTransactionEntryPanel(
                                movieRs.getString("title"),
                                movieRs.getString("poster"),
                                movieRs.getString("age_rating"),
                                rs.getString("transaction_date"),
                                rs.getDouble("amount"), // Lấy amount dưới dạng double
                                rs.getString("seats_preserved")
                        ));
                        panel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
            }
            if (!hasRows) {
                JLabel emptyLabel = new JLabel("No transactions found.");
                emptyLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(Box.createVerticalGlue());
                panel.add(emptyLabel);
                panel.add(Box.createVerticalGlue());
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading transactions: " + e.getMessage());
            errorLabel.setForeground(DANGER_RED); // Use DANGER_RED
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
        }
        return panel;
    }

    private JPanel createTransactionEntryPanel(String movieTitle, String posterUrl, String ageRating, String transactionDate, double amount, String seatsPreserved) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true), // Use BORDER_COLOR
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        ));
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        entryPanel.add(createPosterPanel(movieTitle, posterUrl, ageRating), BorderLayout.WEST);
        entryPanel.add(createDetailsPanel(transactionDate, amount, seatsPreserved), BorderLayout.CENTER); // Truyền amount dưới dạng double
        return entryPanel;
    }

    private JPanel createPosterPanel(String movieTitle, String posterUrl, String ageRating) {
        JPanel posterPanel = new JPanel();
        posterPanel.setLayout(new BoxLayout(posterPanel, BoxLayout.Y_AXIS));
        posterPanel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND

        JLabel posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(80, 120));
        posterLabel.setMaximumSize(new Dimension(80, 120));
        if (posterUrl != null && !posterUrl.isEmpty()) {
            try {
                URL imageUrl = new URI(posterUrl).toURL();
                BufferedImage img = ImageIO.read(imageUrl);
                Image scaledImg = img.getScaledInstance(80, 120, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaledImg));
            } catch (IOException | URISyntaxException e) {
                posterLabel.setText("[No Image]");
                posterLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
            }
        } else {
            posterLabel.setText("[No Image]");
            posterLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        }

        JLabel titleLabel = new JLabel(movieTitle);
        titleLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel ageRatingLabel = new JLabel("Age Rating: " + ageRating);
        ageRatingLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        ageRatingLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        posterPanel.add(posterLabel);
        posterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        posterPanel.add(titleLabel);
        posterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        posterPanel.add(ageRatingLabel);

        return posterPanel;
    }

    private JPanel createDetailsPanel(String transactionDate, double amount, String seatsPreserved) { // Thay đổi kiểu dữ liệu của amount
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND

        JLabel dateLabel = new JLabel("Date: " + transactionDate);
        dateLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel seatsLabel = new JLabel("Seats: " + seatsPreserved);
        seatsLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        seatsLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Định dạng amount thành chuỗi tiền tệ Việt Nam
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormat.format(amount);

        JLabel amountLabel = new JLabel("Amount: " + formattedAmount);
        amountLabel.setForeground(ACCENT_TEAL); // Use ACCENT_TEAL
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        detailsPanel.add(dateLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(seatsLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(amountLabel);

        return detailsPanel;
    }
}
