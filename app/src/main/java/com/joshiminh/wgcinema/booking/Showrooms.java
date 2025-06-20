package com.joshiminh.wgcinema.booking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.net.MalformedURLException;
import java.net.URL;

import com.joshiminh.wgcinema.data.AgeRatingColor;
import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import static com.joshiminh.wgcinema.utils.AgentStyles.*; // Import AgentStyles

public class Showrooms extends JFrame {
    private static final int WIDTH = 1900, HEIGHT = 900, GAP = 3, MAX_SELECTIONS = 8;
    private static int ROWS, COLS, CELL_SIZE, sideWidths;
    private JPanel gridPanel;
    private JLabel infoLabel;
    private JLabel totalPriceLabel;
    private final Set<JPanel> selectedCells = new HashSet<>();
    private String connectionString;
    private int showtimeID, showroomID, movieId;
    private String chairsBooked = "", movieTitle, movieRating, movieLink;
    private Time time;
    private java.sql.Date date;
    private int regularSeatPrice;
    private int vipSeatPrice;

    public Showrooms(String connectionString, int showtimeID) {
        this.showtimeID = showtimeID;
        this.connectionString = connectionString;

        setTitle("Select Seats");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(WIDTH, HEIGHT);
        setResizable(true);
        setIconImage(ResourceUtil.loadAppIcon());

        showroomID = getShowroomID(showtimeID);
        fetchMovieInfo();
        fetchShowtimePrices();
        setDimensions(showroomID);

        gridPanel = new JPanel(new GridLayout(ROWS, COLS, GAP, GAP));
        gridPanel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        createGridOfBoxes(chairsBooked);

        int gridPanelHeight = gridPanel.getPreferredSize().height;
        JPanel topPanel = createTopPanel(gridPanelHeight);
        JPanel leftPanel = createSidePanel(gridPanelHeight, sideWidths);
        JPanel rightPanel = createSidePanel(gridPanelHeight, sideWidths);
        JPanel bottomInfoPanel = createBottomInfoPanel();

        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(gridPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomInfoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private int getShowroomID(int showtimeID) {
        try (ResultSet rs = DAO.fetchShowtimeDetails(connectionString, showtimeID)) {
            if (rs != null && rs.next()) {
                date = rs.getDate("date");
                chairsBooked = rs.getString("chairs_booked");
                time = rs.getTime("time");
                movieId = rs.getInt("movie_id");
                return rs.getInt("showroom_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void fetchMovieInfo() {
        try (ResultSet rs = DAO.fetchMovieDetails(connectionString, movieId)) {
            if (rs != null && rs.next()) {
                movieTitle = rs.getString("title");
                movieRating = rs.getString("age_rating");
                movieLink = rs.getString("poster");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchShowtimePrices() {
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
    }

    private void setDimensions(int showroomID) {
        int totalWidth = 1450;
        try (ResultSet rs = DAO.fetchShowroomDetails(connectionString, showroomID)) {
            if (rs != null && rs.next()) {
                ROWS = rs.getInt("rowCount");
                COLS = rs.getInt("collumnCount");
                int maxChairs = rs.getInt("max_chairs");
                CELL_SIZE = maxChairs > 250 ? 50 : 60;
                sideWidths = totalWidth - (CELL_SIZE * COLS);
            } else {
                defaultDimensions(totalWidth);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            defaultDimensions(totalWidth);
        }
    }

    private void defaultDimensions(int totalWidth) {
        CELL_SIZE = 60;
        ROWS = 10;
        COLS = 10;
        sideWidths = totalWidth - (CELL_SIZE * COLS);
    }

    private JPanel createTopPanel(int gridPanelHeight) {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        topPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT - gridPanelHeight - 140));

        JLabel screenLabel = new JLabel("SCREEN");
        screenLabel.setFont(new Font(screenLabel.getFont().getName(), Font.BOLD, 35));
        screenLabel.setForeground(LIGHT_TEXT_COLOR.darker()); // Use LIGHT_TEXT_COLOR.darker()

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        topPanel.add(screenLabel, gbc);
        return topPanel;
    }

    private JPanel createSidePanel(int height, int width) {
        JPanel panel = new JPanel();
        panel.setBackground(PRIMARY_BACKGROUND); // Use PRIMARY_BACKGROUND
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    private JPanel createBottomInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SECONDARY_BACKGROUND); // Use SECONDARY_BACKGROUND
        panel.setPreferredSize(new Dimension(WIDTH, 140));
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COLOR)); // Use BORDER_COLOR

        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        westPanel.setOpaque(false);

        ImageIcon posterIcon = null;
        try {
            posterIcon = new ImageIcon(new URL(movieLink));
            Image img = posterIcon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
            posterIcon = new ImageIcon(img);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JLabel posterLabel = new JLabel(posterIcon);
        westPanel.add(posterLabel);
        panel.add(westPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 2.0;

        JPanel movieInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        movieInfoPanel.setOpaque(false);

        JLabel movieTitleLabel = new JLabel(movieTitle);
        movieTitleLabel.setForeground(LIGHT_TEXT_COLOR); // Use LIGHT_TEXT_COLOR
        movieInfoPanel.add(movieTitleLabel);

        JLabel movieRatingLabel = new JLabel(" " + movieRating);
        movieRatingLabel.setForeground(AgeRatingColor.getColorForRating(movieRating));
        movieInfoPanel.add(movieRatingLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(movieInfoPanel, gbc);

        JLabel showroomIDLabel = new JLabel("Showroom " + showroomID);
        showroomIDLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridy = 1;
        centerPanel.add(showroomIDLabel, gbc);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String formattedTime = timeFormat.format(time);
        JLabel timeLabel = new JLabel("Time: " + formattedTime);
        timeLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridy = 2;
        centerPanel.add(timeLabel, gbc);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(this.date);
        JLabel dateLabel = new JLabel("Date: " + formattedDate);
        dateLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridy = 3;
        centerPanel.add(dateLabel, gbc);

        totalPriceLabel = new JLabel("Total: " + this.calculateTotalPrice(""));
        totalPriceLabel.setForeground(ACCENT_TEAL); // Use ACCENT_TEAL
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 17));

        infoLabel = new JLabel(updateMessage());
        infoLabel.setForeground(TEXT_COLOR); // Use TEXT_COLOR
        gbc.gridy = 4;
        centerPanel.add(infoLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel(new GridBagLayout());
        eastPanel.setOpaque(false);
        eastPanel.setPreferredSize(new Dimension(160, 140));

        GridBagConstraints gbcPrice = new GridBagConstraints();
        gbcPrice.gridx = 0;
        gbcPrice.gridy = 0;
        gbcPrice.anchor = GridBagConstraints.CENTER;
        gbcPrice.insets = new Insets(0, 0, 10, 0);
        eastPanel.add(totalPriceLabel, gbcPrice);

        JButton bookButton = new JButton("Check Out");
        bookButton.setPreferredSize(new Dimension(120, 40));
        bookButton.setFont(new Font("Arial", Font.BOLD, 17));
        bookButton.setBackground(ACCENT_BLUE); // Use ACCENT_BLUE
        bookButton.setForeground(TEXT_COLOR); // Use TEXT_COLOR

        bookButton.addActionListener(_ -> {
            String selectedSeats = selectedCells.stream()
                    .map(cell -> ((JLabel) cell.getComponent(0)).getText())
                    .collect(Collectors.joining(", "));
            if (selectedCells.isEmpty()) {
                JOptionPane.showMessageDialog(Showrooms.this, "Please select at least one seat.", "No Seats Selected", JOptionPane.ERROR_MESSAGE);
            } else {
                new Checkout(connectionString, showroomID, time, movieId, date, movieTitle, movieRating, movieLink, showtimeID, selectedSeats, this);
            }
        });
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

        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 0;
        gbcButton.gridy = 1;
        gbcButton.anchor = GridBagConstraints.CENTER;
        eastPanel.add(bookButton, gbcButton);

        panel.add(eastPanel, BorderLayout.EAST);
        return panel;
    }

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

    private String updateMessage() {
        if (selectedCells.isEmpty()) {
            totalPriceLabel.setText("Total: " + this.calculateTotalPrice(""));
            return "No seats selected";
        }
        List<String> selectedSeatsList = new ArrayList<>();
        for (JPanel cell : selectedCells) {
            selectedSeatsList.add(((JLabel) cell.getComponent(0)).getText());
        }
        String sortedSelectedSeats = sortSelectedSeats(String.join(", ", selectedSeatsList));

        totalPriceLabel.setText("Total: " + this.calculateTotalPrice(sortedSelectedSeats));

        return "You have selected " + selectedCells.size() + " seats: " + sortedSelectedSeats;
    }

    public static String sortSelectedSeats(String selectedSeats) {
        String[] seats = selectedSeats.split(", ");
        Arrays.sort(seats, (s1, s2) -> {
            char row1 = s1.charAt(0);
            char row2 = s2.charAt(0);
            if (row1 != row2) return Character.compare(row1, row2);
            int number1 = Integer.parseInt(s1.substring(1));
            int number2 = Integer.parseInt(s2.substring(1));
            return Integer.compare(number1, number2);
        });
        return String.join(", ", seats);
    }

    private boolean isSeatBooked(String seat) {
        String pattern = "\\b" + seat + "\\b";
        return chairsBooked.matches(".*" + pattern + ".*");
    }

    private void createGridOfBoxes(String chairsBooked) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JPanel box = new JPanel();
                String seatLabel = getBoxLabel(row, col);
                boolean isBooked = isSeatBooked(seatLabel);

                // Define colors based on new palette
                Color vipBookedColor = ACCENT_BLUE.darker().darker(); // Darker blue for VIP booked
                Color regularBookedColor = LIGHT_TEXT_COLOR.darker(); // Darker grey for regular booked
                Color vipAvailableColor = ACCENT_BLUE.darker(); // Darker blue for VIP available
                Color regularAvailableColor = SECONDARY_BACKGROUND; // Secondary background for regular available

                Color currentColor = isBooked ? (isVIPRow(row) ? vipBookedColor : regularBookedColor) :
                        (isVIPRow(row) ? vipAvailableColor : regularAvailableColor);

                box.setBackground(currentColor);
                box.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); // Use BORDER_COLOR

                JLabel label = new JLabel(seatLabel);
                label.setForeground(isBooked ? LIGHT_TEXT_COLOR : TEXT_COLOR); // Use TEXT_COLOR and LIGHT_TEXT_COLOR
                label.setHorizontalAlignment(JLabel.CENTER);
                box.add(label);

                if (!isBooked) box.addMouseListener(new BoxClickListener(box));
                gridPanel.add(box);
            }
        }
    }

    private boolean isVIPRow(int row) {
        // Assuming rows G to L are VIP. 'A' is row 0, 'G' is row 6.
        return row >= 6 && row <= 11;
    }

    private String getBoxLabel(int row, int col) {
        char rowChar = (char) ('A' + row);
        int colNum = COLS - col; // Assuming columns are numbered from right to left
        return rowChar + String.valueOf(colNum);
    }

    private class BoxClickListener extends MouseAdapter {
        private final JPanel box;

        BoxClickListener(JPanel box) {
            this.box = box;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (selectedCells.contains(box)) {
                selectedCells.remove(box);
                // Reset color based on VIP/Regular and available state
                JLabel label = (JLabel) box.getComponent(0);
                String seatLabel = label.getText();
                char rowChar = seatLabel.charAt(0);
                int row = rowChar - 'A';
                box.setBackground(isVIPRow(row) ? ACCENT_BLUE.darker() : SECONDARY_BACKGROUND);
                box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); // Use BORDER_COLOR
            } else if (selectedCells.size() < MAX_SELECTIONS) {
                selectedCells.add(box);
                box.setBorder(BorderFactory.createLineBorder(ACCENT_TEAL, 2)); // Use ACCENT_TEAL for selection border
            } else {
                JOptionPane.showMessageDialog(Showrooms.this, "You have selected the maximum number of seats (" + MAX_SELECTIONS + ").", "Selection Limit Reached", JOptionPane.WARNING_MESSAGE);
            }
            infoLabel.setText(updateMessage());
        }
    }

    public void restartShowrooms() {
        dispose();
        new Showrooms(connectionString, showtimeID).setVisible(true);
    }
}
