package com.joshiminh.wgcinema.booking;

import javax.swing.*;
import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static com.joshiminh.wgcinema.utils.AgentStyles.*; // Import AgentStyles

public class Checkout extends JFrame {
    private static final int WIDTH = 400, HEIGHT = 700;
    private int regularSeatPrice;
    private int vipSeatPrice;

    private final JLabel selectedSeatsLabel;
    private int showtimeID, showroomID, movieId;
    private Showrooms showroomsFrame;
    private boolean bookingSuccessful = false;
    private String connectionString;
    private String initialSelectedSeats;

    public Checkout(String connectionString, int showroomID, Time time, int movieId, Date date, String movieTitle, String movieRating, String movieLink, int showtimeID, String selectedSeats, Showrooms showroomsFrame) {
        this.showtimeID = showtimeID;
        this.showroomsFrame = showroomsFrame;
        this.showroomID = showroomID;
        this.movieId = movieId;
        this.connectionString = connectionString;
        this.initialSelectedSeats = selectedSeats;

        try (ResultSet rs = DAO.fetchShowtimeDetails(connectionString, showtimeID)) {
            if (rs != null && rs.next()) {
                this.regularSeatPrice = rs.getInt("regular_price");
                this.vipSeatPrice = rs.getInt("vip_price");
            } else {
                this.regularSeatPrice = 80000;
                this.vipSeatPrice = 85000;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.regularSeatPrice = 80000;
            this.vipSeatPrice = 85000;
        }

        setTitle("Checkout");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        setIconImage(ResourceUtil.loadAppIcon());

        JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        northPanel.setPreferredSize(new Dimension(WIDTH, 130));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel moviePosterLabel = new JLabel();
        try {
            URL moviePosterUrl = new URL(movieLink);
            ImageIcon moviePosterIcon = new ImageIcon(moviePosterUrl);
            Image scaledMoviePosterImage = moviePosterIcon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
            moviePosterLabel.setIcon(new ImageIcon(scaledMoviePosterImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 5; gbc.anchor = GridBagConstraints.NORTHWEST;
        northPanel.add(moviePosterLabel, gbc);
        gbc.gridheight = 1;

        JLabel movieLabel = new JLabel(movieTitle);
        movieLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1;
        northPanel.add(movieLabel, gbc);

        Color ratingColor = AgeRatingColor.getColorForRating(movieRating);
        JLabel ratingLabel = new JLabel(movieRating);
        ratingLabel.setForeground(ratingColor);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1;
        northPanel.add(ratingLabel, gbc);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        JLabel dateLabel = new JLabel("Date: " + dateFormat.format(date));
        dateLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        northPanel.add(dateLabel, gbc);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        JLabel timeLabel = new JLabel("Time: " + timeFormat.format(time));
        timeLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        northPanel.add(timeLabel, gbc);

        JLabel showroomLabel = new JLabel("Showroom " + showroomID);
        showroomLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        northPanel.add(showroomLabel, gbc);

        selectedSeatsLabel = new JLabel("Selected Seats: " + selectedSeats);
        selectedSeatsLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        northPanel.add(selectedSeatsLabel, gbc);

        add(northPanel, BorderLayout.NORTH);

        JButton bookButton = new JButton("CONFIRM");
        bookButton.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        bookButton.setBackground(ACCENT_BLUE); // Use ACCENT_BLUE
        bookButton.setFont(bookButton.getFont().deriveFont(Font.BOLD, 25));
        bookButton.addActionListener(_ -> book());
        bookButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bookButton.setBackground(ACCENT_BLUE.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bookButton.setBackground(ACCENT_BLUE);
            }
        });

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setPreferredSize(new Dimension(50, 60));
        southPanel.add(bookButton, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(false);

        JPanel northCenterPanel = new JPanel();
        northCenterPanel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND
        northCenterPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel priceLabel = new JLabel("Total Price: " + calculateTotalPrice(initialSelectedSeats));
        priceLabel.setForeground(ACCENT_TEAL); // Use ACCENT_TEAL
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 15));
        northCenterPanel.add(priceLabel);
        innerPanel.add(northCenterPanel, BorderLayout.NORTH);

        ImageIcon qrIcon = new ImageIcon(ResourceUtil.loadImage("/images/QR.jpg"));
        JLabel qrLabel = new JLabel(qrIcon);
        innerPanel.add(qrLabel, BorderLayout.CENTER);

        JPanel southCenterPanel = new JPanel();
        southCenterPanel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND
        southCenterPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel scanLabel = new JLabel("SCAN TO PAY");
        scanLabel.setForeground(ACCENT_TEAL); // Use ACCENT_TEAL
        scanLabel.setFont(scanLabel.getFont().deriveFont(Font.BOLD, 20));
        southCenterPanel.add(scanLabel);
        innerPanel.add(southCenterPanel, BorderLayout.SOUTH);

        add(innerPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (bookingSuccessful) {
                    // If booking was successful, Showrooms frame will be restarted by the book() method
                    // No need to restart here again, just dispose this frame.
                } else {
                    // If booking was NOT successful (e.g., user closed checkout without confirming, or conflict),
                    // ensure the Showrooms frame is still visible and potentially refreshed if needed.
                    // For now, we assume Showrooms is already open and will be refreshed by book() on conflict.
                    // If user just closes Checkout, Showrooms remains as is.
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // This method is now moved to DAO.java for better encapsulation of database logic.
    // public static boolean checkBooked(String chairsBooked, String selectedSeats) { ... }

    public String calculateTotalPrice(String selectedSeats) {
        int totalPrice = 0;
        if (selectedSeats == null || selectedSeats.trim().isEmpty()) {
            return formatPrice(0);
        }
        String[] seats = selectedSeats.split(", ");
        for (String seat : seats) {
            if (seat.trim().isEmpty()) {
                continue;
            }
            char row = seat.charAt(0);
            if (row >= 'A' && row <= 'F') totalPrice += this.regularSeatPrice;
            else if (row >= 'G' && row <= 'L') totalPrice += this.vipSeatPrice;
        }
        return formatPrice(totalPrice);
    }

    private static String formatPrice(int price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + "vnđ";
    }

    private void book() {
        try {
            String selectedSeatsToBook = initialSelectedSeats;
            int reservedCount = selectedSeatsToBook.split(", ").length;

            // Call the modified DAO method which handles concurrency
            // It returns 1 for success, -1 for conflict (seats already booked), 0 for other DB errors
            int updatedRows = DAO.updateShowtimeSeats(connectionString, reservedCount, selectedSeatsToBook, showtimeID);

            if (updatedRows > 0) {
                String userEmail = "";
                java.nio.file.Path USER_FILE = java.nio.file.Paths.get("user.txt");
                java.util.List<String> lines = java.nio.file.Files.readAllLines(USER_FILE);
                if (!lines.isEmpty()) userEmail = lines.get(0).trim();

                JOptionPane.showMessageDialog(this, "Booking Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                showSuccessImage();
                bookingSuccessful = true;
                DAO.insertTransaction(connectionString, movieId, calculateTotalPrice(selectedSeatsToBook), selectedSeatsToBook, showroomID, userEmail, showtimeID);

                // Restart Showrooms frame to show updated seat status
                if (showroomsFrame != null) {
                    showroomsFrame.restartShowrooms();
                }
                dispose(); // Close the Checkout frame
            } else if (updatedRows == -1) {
                // This means seats were already booked by another concurrent transaction
                JOptionPane.showMessageDialog(this, "Some selected seats have just been booked by another user. Please select different seats.", "Seat Already Booked", JOptionPane.WARNING_MESSAGE);

                // Restart Showrooms frame to show updated seat status
                if (showroomsFrame != null) {
                    showroomsFrame.restartShowrooms();
                }
                dispose(); // Close the Checkout frame
            } else {
                // General database error (updatedRows == 0)
                JOptionPane.showMessageDialog(this, "Failed to book seats due to a database error. Please try again.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSuccessImage() {
        getContentPane().removeAll();
        revalidate();
        repaint();
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon(ResourceUtil.loadImage("/images/TicketBooked.png"));
        Image scaledImage = imageIcon.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));
        centerPanel.add(imageLabel, new GridBagConstraints());
        add(centerPanel, BorderLayout.CENTER);
        getContentPane().setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        revalidate();
        repaint();
    }
}
