package com.joshiminh.wgcinema.booking;

import com.joshiminh.wgcinema.data.DAO;
import com.joshiminh.wgcinema.utils.ResourceUtil;
import com.joshiminh.wgcinema.utils.AgentStyles; // Import AgentStyles

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Booking extends JFrame {
    private static final Color BG_COLOR = AgentStyles.PRIMARY_BACKGROUND;
    private static final Color ACCENT_COLOR = AgentStyles.ACCENT_BLUE;
    private static final Font BASE_FONT = AgentStyles.LABEL_FONT; // Use AgentStyles font
    private static final Font HEADER_FONT = AgentStyles.TITLE_FONT.deriveFont(Font.BOLD, 28f); // Larger header font
    private static final Font TAB_FONT = BASE_FONT.deriveFont(Font.BOLD, 16f);
    private static final Font BUTTON_FONT = BASE_FONT.deriveFont(Font.BOLD, 16f);
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final Dimension BUTTON_SIZE = new Dimension(100, 45); // Slightly larger buttons
    private static final int WINDOW_WIDTH = 750; // Slightly wider window
    private static final int WINDOW_HEIGHT = 480; // Slightly taller window
    private static final int FLOW_GAP_H = 20; // Increased horizontal gap
    private static final int FLOW_GAP_V = 25; // Increased vertical gap

    private final int movieId;
    private final String connStr;

    public Booking(int movieId, String connStr) {
        this.movieId = movieId;
        this.connStr = connStr;
        initFrame();
    }

    private void initFrame() {
        setTitle("Book Tickets");
        setIconImage(ResourceUtil.loadAppIcon());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setContentPane(buildMainPanel());
        setVisible(true);
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(createHeaderPanel(), BorderLayout.NORTH); // Use a panel for the header
        panel.add(buildTabs(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(AgentStyles.SECONDARY_BACKGROUND); // Use secondary background for header
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0)); // More padding

        JLabel header = new JLabel("Select Showtime");
        header.setFont(HEADER_FONT);
        header.setForeground(ACCENT_COLOR);
        headerPanel.add(header);
        return headerPanel;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(AgentStyles.PRIMARY_BACKGROUND);
        tabs.setForeground(AgentStyles.LIGHT_TEXT_COLOR); // Default tab text color
        tabs.setFont(TAB_FONT);

        // Custom UI for tabs to match dark theme
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                UIManager.put("TabbedPane.selected", AgentStyles.SECONDARY_BACKGROUND);
                UIManager.put("TabbedPane.contentAreaColor", AgentStyles.PRIMARY_BACKGROUND);
                UIManager.put("TabbedPane.shadow", AgentStyles.PRIMARY_BACKGROUND);
                UIManager.put("TabbedPane.darkShadow", AgentStyles.PRIMARY_BACKGROUND);
                UIManager.put("TabbedPane.light", AgentStyles.PRIMARY_BACKGROUND);
                UIManager.put("TabbedPane.highlight", AgentStyles.PRIMARY_BACKGROUND);
                UIManager.put("TabbedPane.border", BorderFactory.createLineBorder(AgentStyles.BORDER_COLOR));
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(AgentStyles.SECONDARY_BACKGROUND); // Selected tab color
                } else {
                    g2.setColor(AgentStyles.PRIMARY_BACKGROUND.darker()); // Unselected tab color
                }
                g2.fillRect(x, y, w, h);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // No border for tabs
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // No content border
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                if (isSelected) {
                    g.setColor(AgentStyles.ACCENT_BLUE); // Selected tab text color
                } else {
                    g.setColor(AgentStyles.LIGHT_TEXT_COLOR); // Unselected tab text color
                }
                int x = textRect.x + (textRect.width - metrics.stringWidth(title)) / 2;
                int y = textRect.y + ((textRect.height - metrics.getHeight()) / 2) + metrics.getAscent();
                g.drawString(title, x, y);
            }
        });


        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            Date date = cal.getTime();
            String title = i == 0 ? "Today" : cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            tabs.addTab(title, buildDayPanel(date, i == 0));
            cal.add(Calendar.DATE, 1);
        }
        return tabs;
    }

    private JPanel buildDayPanel(Date date, boolean isToday) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, FLOW_GAP_H, FLOW_GAP_V));
        panel.setBackground(AgentStyles.PRIMARY_BACKGROUND);
        loadShowtimes(panel, date, isToday);
        return panel;
    }

    private void loadShowtimes(JPanel container, Date date, boolean isToday) {
        try (ResultSet rs = DAO.fetchMovieShowtimes(connStr, movieId, DATE_FMT.format(date))) {
            Calendar currentDateTimeCal = Calendar.getInstance();

            boolean showtimesFound = false;
            while (rs != null && rs.next()) {
                String showtimeStr = rs.getString("Time (HH:mm)");

                Calendar showtimeDateTimeCal = Calendar.getInstance();
                showtimeDateTimeCal.setTime(date);
                Date parsedTimeOnly = TIME_FMT.parse(showtimeStr);
                Calendar tempTimeCal = Calendar.getInstance();
                tempTimeCal.setTime(parsedTimeOnly);

                showtimeDateTimeCal.set(Calendar.HOUR_OF_DAY, tempTimeCal.get(Calendar.HOUR_OF_DAY));
                showtimeDateTimeCal.set(Calendar.MINUTE, tempTimeCal.get(Calendar.MINUTE));
                showtimeDateTimeCal.set(Calendar.SECOND, 0);
                showtimeDateTimeCal.set(Calendar.MILLISECOND, 0);

                Date fullShowtimeDate = showtimeDateTimeCal.getTime();

                if (!isToday || fullShowtimeDate.after(currentDateTimeCal.getTime())) {
                    container.add(createButton(showtimeStr, rs.getInt("showtime_id")));
                    showtimesFound = true;
                }
            }

            if (!showtimesFound) {
                JLabel noShowtimeLabel = new JLabel("No showtimes available for this day.");
                noShowtimeLabel.setForeground(AgentStyles.LIGHT_TEXT_COLOR);
                noShowtimeLabel.setFont(BASE_FONT.deriveFont(Font.ITALIC, 16f));
                container.add(noShowtimeLabel);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading showtimes.");
            errorLabel.setForeground(AgentStyles.DANGER_RED);
            errorLabel.setFont(BASE_FONT.deriveFont(Font.ITALIC, 16f));
            container.add(errorLabel);
        }
    }

    private JButton createButton(String text, int showtimeId) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setFont(BUTTON_FONT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        AgentStyles.styleButton(btn); // Use AgentStyles.styleButton for consistent styling
        btn.addActionListener(_ -> SwingUtilities.invokeLater(() -> new Showrooms(connStr, showtimeId)));
        return btn;
    }
}