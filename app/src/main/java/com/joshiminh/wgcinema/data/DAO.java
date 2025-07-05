package com.joshiminh.wgcinema.data;

import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class DAO {
    // ==========================
    // SELECT Operations
    // ==========================

    // Movie-related SELECT operations
    public static ResultSet fetchMovieDetails(String connectionString, int movieId) {
        // Updated to select description and trailer_url
        String sql = "SELECT * FROM movies WHERE id = ?";
        return select(connectionString, sql, movieId);
    }

    public static ResultSet fetchAllMovies(String connectionString) {
        String sql = "SELECT id, title, age_rating, release_date FROM movies ORDER BY release_date";
        return select(connectionString, sql);
    }

    public static ResultSet fetchUpcomingMovies(String connectionString) {
        String sql = """
         SELECT id, poster, title, age_rating, director, duration
         FROM movies
         WHERE release_date >= CURDATE()
         ORDER BY release_date ASC
         """;
        return select(connectionString, sql);
    }

    public static ResultSet fetchAgeRatingCounts(String connectionString) {
        String sql = "SELECT age_rating, COUNT(*) AS count FROM movies GROUP BY age_rating";
        return select(connectionString, sql);
    }

    public static ResultSet fetchLanguageCounts(String connectionString) {
        String sql = "SELECT language, COUNT(*) AS count FROM movies GROUP BY language";
        return select(connectionString, sql);
    }

    public static ResultSet searchMoviesByTitle(String connectionString, String titleQuery) {
        // FIX: Added poster, director, and duration to the SELECT statement
        String sql = "SELECT id, poster, title, age_rating, director, duration FROM movies WHERE title LIKE ? ORDER BY release_date LIMIT 20";
        return select(connectionString, sql, "%" + titleQuery + "%");
    }

    // Transaction-related SELECT operations
    public static ResultSet fetchTransactionHistory(String connectionString, String email) {
        String sql = "SELECT * FROM transactions WHERE account_email = ? ORDER BY transaction_date DESC";
        return select(connectionString, sql, email);
    }

    // Showtime-related SELECT operations
    public static ResultSet fetchShowtimeDetails(String connectionString, int showtimeId) {
        // First try with new columns, fall back to old query if columns don't exist
        String sql = "SELECT *, regular_price, vip_price FROM showtimes WHERE showtime_id = ?";
        try {
            ResultSet rs = select(connectionString, sql, showtimeId);
            if (rs != null && rs.next()) {
                // Check if new columns exist by trying to access them
                try {
                    rs.getString("chairs_selecting");
                    // If we get here, new columns exist, reset cursor and return
                    rs.beforeFirst();
                    return rs;
                } catch (SQLException e) {
                    // New columns don't exist, use old query
                    rs.close();
                }
            }
        } catch (SQLException e) {
            // Fall back to basic query
        }

        // Return basic query result
        return select(connectionString, sql, showtimeId);
    }

    // NEW: Get all seats currently being selected for a showtime
    public static String getSelectingSeats(String connectionString, int showtimeId) {
        String sql = "SELECT selected_seats FROM seat_selections WHERE showtime_id = ?";
        StringBuilder allSelectingSeats = new StringBuilder();

        try (ResultSet rs = select(connectionString, sql, showtimeId)) {
            while (rs != null && rs.next()) {
                String seats = rs.getString("selected_seats");
                if (seats != null && !seats.trim().isEmpty()) {
                    if (allSelectingSeats.length() > 0) {
                        allSelectingSeats.append(" ");
                    }
                    allSelectingSeats.append(seats);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting selecting seats: " + e.getMessage());
        }

        return allSelectingSeats.toString();
    }

    // REMOVED: cleanupExpiredSelections method is no longer needed for automatic timeout.
    // public static void cleanupExpiredSelections(String connectionString) {
    //     String sql = "DELETE FROM seat_selections WHERE selection_timestamp < DATE_SUB(NOW(), INTERVAL 5 MINUTE)";
    //     try {
    //         int deleted = update(connectionString, sql);
    //         if (deleted > 0) {
    //             System.out.println("Cleaned up " + deleted + " expired seat selections");
    //         }
    //     } catch (Exception e) {
    //         System.err.println("Error cleaning up expired selections: " + e.getMessage());
    //     }
    // }

    public static ResultSet fetchMovieShowtimes(String connectionString, int movieId, String date) {
        String sql = """
         SELECT showtime_id, TIME_FORMAT(time, '%H:%i') AS 'Time (HH:mm)'
         FROM showtimes
         WHERE movie_id = ? AND date = ?
         ORDER BY time
         """;
        return select(connectionString, sql, movieId, date);
    }

    public static ResultSet fetchAllShowtimes(String connectionString) {
        String sql = "SELECT * FROM showtimes";
        return select(connectionString, sql);
    }

    // Showroom-related SELECT operations
    public static ResultSet fetchShowroomDetails(String connectionString, int showroomId) {
        String sql = "SELECT * FROM showrooms WHERE showroom_id = ?";
        return select(connectionString, sql, showroomId);
    }

    public static ResultSet fetchAllShowrooms(String connectionString) {
        String sql = "SELECT * FROM showrooms";
        return select(connectionString, sql);
    }

    // Account-related SELECT operations
    public static ResultSet fetchAccountByEmail(String connectionString, String email) {
        String sql = "SELECT * FROM accounts WHERE account_email = ?";
        return select(connectionString, sql, email);
    }

    // Employee-related SELECT operations
    // Lấy tất cả tài khoản (coi như tất cả đều là nhân viên cho mục đích hiển thị này)
    public static ResultSet fetchEmployees(String connectionString) {
        // Lấy email và tên từ bảng accounts
        String sql = "SELECT account_email, name FROM accounts ORDER BY name";
        return select(connectionString, sql);
    }

    // Lấy tất cả các giao dịch (amount và seats_preserved) được xử lý bởi một nhân viên cụ thể
    // Dựa trên cột 'account_email' trong bảng 'transactions'
    public static ResultSet fetchEmployeeTransactions(String connectionString, String employeeEmail) {
        // FIX: Changed query to fetch amount and seats_preserved for each transaction
        String sql = """
         SELECT amount, seats_preserved
         FROM transactions
         WHERE account_email = ?
         """;
        return select(connectionString, sql, employeeEmail);
    }

    // NEW: Fetch employees with their monthly sales data
    public static ResultSet fetchEmployeesWithSalesData(String connectionString) {
        String sql = "SELECT account_email, name, total_tickets_sold_current_month, total_revenue_current_month, total_tickets_sold_last_month, total_revenue_last_month, last_reset_date FROM accounts ORDER BY name";
        return select(connectionString, sql);
    }

    // Utility SELECT operations
    public static String[] getColumnNames(String connectionString, String table) {
        try (Connection con = DriverManager.getConnection(connectionString)) {
            ResultSet rs = con.getMetaData().getColumns(null, null, table, null);
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString("COLUMN_NAME"));
            }
            return names.toArray(new String[0]);
        } catch (SQLException e) {
            return new String[0];
        }
    }


    // ==========================
    // INSERT Operations
    // ==========================

    public static int insertTransaction(String connectionString, int movieId, String totalPrice, String selectedSeats, int showroomId, String accountEmail, int showtimeId) {
        String sql = """
         INSERT INTO transactions (movie_id, amount, seats_preserved, showroom_id, account_email, showtime_id)
         VALUES (?, ?, ?, ?, ?, ?)
         """;
        try {
            // Parse the price string using Vietnamese locale to correctly handle thousands separator
            NumberFormat parser = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            Number parsedNumber = parser.parse(totalPrice.replace("vnđ", "").trim());
            java.math.BigDecimal amount = new java.math.BigDecimal(parsedNumber.doubleValue());
            System.out.println("DAO insertTransaction: Amount to insert (BigDecimal): " + amount);
            return update(
                    connectionString,
                    sql,
                    movieId,
                    amount,
                    selectedSeats,
                    showroomId,
                    accountEmail,
                    showtimeId
            );
        } catch (ParseException e) {
            System.err.println("Error parsing price: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static int insertTransactionReturnId(
            String connectionString,
            int movieId,
            String totalPrice,
            String selectedSeats,
            int showroomId,
            String accountEmail,
            int showtimeId) throws SQLException {

        String sql = """
     INSERT INTO transactions
       (movie_id, amount, seats_preserved, showroom_id, account_email, showtime_id)
     VALUES (?, ?, ?, ?, ?, ?)
     """;

        try (
                Connection conn = DriverManager.getConnection(connectionString);
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            // Parse the price string using Vietnamese locale to correctly handle thousands separator
            NumberFormat parser = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            Number parsedNumber = parser.parse(totalPrice.replace("vnđ", "").trim());
            java.math.BigDecimal amount = new java.math.BigDecimal(parsedNumber.doubleValue());
            ps.setInt(1, movieId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, selectedSeats);
            ps.setInt(4, showroomId);
            ps.setString(5, accountEmail);
            ps.setInt(6, showtimeId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        } catch (ParseException e) {
            throw new SQLException("Error parsing price: " + e.getMessage(), e);
        }
    }

    // NEW: Insert seat selection (temporary)
    public static int insertSeatSelection(String connectionString, int showtimeId, String userEmail, String selectedSeats) {
        // REMOVED: No longer calling cleanupExpiredSelections here

        String sql = """
         INSERT INTO seat_selections (showtime_id, user_email, selected_seats)
         VALUES (?, ?, ?)
         ON DUPLICATE KEY UPDATE 
         selected_seats = VALUES(selected_seats),
         selection_timestamp = CURRENT_TIMESTAMP
         """;
        return update(connectionString, sql, showtimeId, userEmail, selectedSeats);
    }

    public static int insertMovie(String connectionString, String[] columns, String[] values) {
        String sql = "INSERT INTO movies (" + String.join(", ", columns) + ") VALUES (" +
                "?,".repeat(values.length).replaceAll(",$", "") + ")";
        return update(connectionString, sql, (Object[]) values);
    }

    public static int insertShowtime(String connectionString, String[] columns, String[] values) {
        String sql = "INSERT INTO showtimes (" + String.join(", ", columns) + ") VALUES (" +
                "?,".repeat(values.length).replaceAll(",$", "") + ")";
        return update(connectionString, sql, (Object[]) values);
    }

    public static int insertShowroom(String connectionString, String[] columns, String[] values) {
        String sql = "INSERT INTO showrooms (" + String.join(", ", columns) + ") VALUES (" +
                "?,".repeat(columns.length).replaceAll(",$", "") + ")";
        return update(connectionString, sql, (Object[]) values);
    }

    // ==========================
    // UPDATE Operations
    // ==========================

    public static int updateShowtimeSeats(String connectionString, int reservedCount, String selectedSeats, int showtimeId) {
        String sql = """
         UPDATE showtimes
         SET reserved_chairs = reserved_chairs + ?, chairs_booked = CONCAT(chairs_booked, ?)
         WHERE showtime_id = ?
         """;
        return update(connectionString, sql, reservedCount, " " + selectedSeats, showtimeId);
    }

    // NEW: Update seat selection for a user
    public static int updateSeatSelection(String connectionString, int showtimeId, String userEmail, String selectedSeats) {
        // REMOVED: No longer calling cleanupExpiredSelections here

        // First try to update existing record
        String updateSql = """
         UPDATE seat_selections 
         SET selected_seats = ?, selection_timestamp = CURRENT_TIMESTAMP
         WHERE showtime_id = ? AND user_email = ?
         """;
        int updated = update(connectionString, updateSql, selectedSeats, showtimeId, userEmail);

        // If no record was updated, insert new one
        if (updated == 0) {
            return insertSeatSelection(connectionString, showtimeId, userEmail, selectedSeats);
        }
        return updated;
    }

    public static int updateShowtimeColumn(String connectionString, String columnName, Object value, Object showtimeId) {
        String sql = "UPDATE showtimes SET " + columnName + " = ? WHERE showtime_id = ?";
        return update(connectionString, sql, value, showtimeId);
    }

    public static int updateShowroomColumn(String connectionString, String columnName, Object value, Object showroomId) {
        String sql = "UPDATE showrooms SET " + columnName + " = ? WHERE showroom_id = ?";
        return update(connectionString, sql, value, showroomId);
    }

    public static int updateMovieById(String connectionString, String[] columns, String[] values, int movieId) {
        StringBuilder sql = new StringBuilder("UPDATE movies SET ");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]).append(" = ?");
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(" WHERE id = ?");
        Object[] params = new Object[values.length + 1];
        System.arraycopy(values, 0, params, 0, values.length);
        params[values.length] = movieId;
        return update(connectionString, sql.toString(), params);
    }

    public static int updateAccountPassword(String connectionString, String email, String newHashedPassword) {
        String sql = "UPDATE accounts SET password_hash = ? WHERE account_email = ?";
        return update(connectionString, sql, newHashedPassword, email);
    }

    public static int updateAccount(String connectionString, String email, String name, String gender, java.sql.Date dateOfBirth, boolean isAdmin) {
        String sql = "UPDATE accounts SET name = ?, gender = ?, date_of_birth = ?, admin = ? WHERE account_email = ?";
        int adminValue = isAdmin ? 1 : 0;
        return update(connectionString, sql, name, gender, dateOfBirth, adminValue, email);
    }

    // NEW: Update current month sales for an account
    public static int updateAccountCurrentMonthSales(String connectionString, String email, int ticketsAdded, double revenueAdded) {
        String sql = "UPDATE accounts SET total_tickets_sold_current_month = total_tickets_sold_current_month + ?, total_revenue_current_month = total_revenue_current_month + ? WHERE account_email = ?";
        System.out.println("DAO updateAccountCurrentMonthSales: Adding tickets=" + ticketsAdded + ", revenue=" + revenueAdded + " for " + email);
        return update(connectionString, sql, ticketsAdded, revenueAdded, email);
    }

    // NEW: Reset monthly sales and move current month to last month
    public static void resetMonthlySales(String connectionString) {
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            LocalDate currentDate = LocalDate.now();
            LocalDate previousMonthDate = currentDate.minusMonths(1);

            // Get start and end of previous month
            LocalDate prevMonthStart = previousMonthDate.withDayOfMonth(1);
            LocalDate prevMonthEnd = previousMonthDate.withDayOfMonth(previousMonthDate.lengthOfMonth());

            // Get start and end of current month
            LocalDate currentMonthStart = currentDate.withDayOfMonth(1);
            // For current month, we calculate up to the current date, not end of month
            LocalDate currentMonthCalcEnd = currentDate;

            System.out.println("DAO resetMonthlySales: Current Date: " + currentDate);
            System.out.println("DAO resetMonthlySales: Previous Month Range: " + prevMonthStart + " to " + prevMonthEnd);
            System.out.println("DAO resetMonthlySales: Current Month Calculation Range: " + currentMonthStart + " to " + currentMonthCalcEnd);

            String fetchAccountsSql = "SELECT account_email, last_reset_date FROM accounts";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(fetchAccountsSql)) {

                while (rs.next()) {
                    String email = rs.getString("account_email");
                    java.sql.Date lastResetDateSql = rs.getDate("last_reset_date");
                    LocalDate lastResetDate = lastResetDateSql != null ? lastResetDateSql.toLocalDate() : null;

                    boolean needsReset = false;
                    if (lastResetDate == null) {
                        needsReset = true; // Never reset before
                        System.out.println("DAO resetMonthlySales: " + email + " needs reset (never reset).");
                    } else if (lastResetDate.getMonth() != currentDate.getMonth() || lastResetDate.getYear() != currentDate.getYear()) {
                        needsReset = true; // Month has changed
                        System.out.println("DAO resetMonthlySales: " + email + " needs reset (month changed from " + lastResetDate.getMonth() + " to " + currentDate.getMonth() + ").");
                    } else {
                        System.out.println("DAO resetMonthlySales: " + email + " does NOT need reset (same month).");
                    }

                    if (needsReset) {
                        // Calculate sales for the PREVIOUS month from transactions
                        int prevMonthTickets = 0;
                        double prevMonthRevenue = 0.0;
                        String calculatePrevMonthSalesSql = """
                         SELECT COUNT(*) AS total_tickets, SUM(amount) AS total_revenue
                         FROM transactions
                         WHERE account_email = ?
                         AND transaction_date >= ? AND transaction_date <= ?
                         """;
                        try (PreparedStatement ps = conn.prepareStatement(calculatePrevMonthSalesSql)) {
                            ps.setString(1, email);
                            ps.setDate(2, java.sql.Date.valueOf(prevMonthStart));
                            ps.setDate(3, java.sql.Date.valueOf(prevMonthEnd));
                            try (ResultSet salesRs = ps.executeQuery()) {
                                if (salesRs.next()) {
                                    prevMonthTickets = salesRs.getInt("total_tickets");
                                    prevMonthRevenue = salesRs.getDouble("total_revenue");
                                }
                            }
                        }
                        System.out.println("DAO resetMonthlySales: " + email + " - Calculated Previous Month Sales: Tickets=" + prevMonthTickets + ", Revenue=" + prevMonthRevenue);

                        // Calculate sales for the CURRENT month from transactions (up to current date)
                        int currentMonthTickets = 0;
                        double currentMonthRevenue = 0.0;
                        String calculateCurrentMonthSalesSql = """
                         SELECT COUNT(*) AS total_tickets, SUM(amount) AS total_revenue
                         FROM transactions
                         WHERE account_email = ?
                         AND transaction_date >= ? AND transaction_date <= ?
                         """;
                        try (PreparedStatement ps = conn.prepareStatement(calculateCurrentMonthSalesSql)) {
                            ps.setString(1, email);
                            ps.setDate(2, java.sql.Date.valueOf(currentMonthStart));
                            ps.setDate(3, java.sql.Date.valueOf(currentMonthCalcEnd)); // Up to current date
                            try (ResultSet salesRs = ps.executeQuery()) {
                                if (salesRs.next()) {
                                    currentMonthTickets = salesRs.getInt("total_tickets");
                                    currentMonthRevenue = salesRs.getDouble("total_revenue");
                                }
                            }
                        }
                        System.out.println("DAO resetMonthlySales: " + email + " - Calculated Current Month Sales: Tickets=" + currentMonthTickets + ", Revenue=" + currentMonthRevenue);

                        // Update the accounts table
                        String updateAccountSql = """
                         UPDATE accounts
                         SET total_tickets_sold_last_month = ?,
                             total_revenue_last_month = ?,
                             total_tickets_sold_current_month = ?,
                             total_revenue_current_month = ?,
                             last_reset_date = ?
                         WHERE account_email = ?
                         """;
                        try (PreparedStatement ps = conn.prepareStatement(updateAccountSql)) {
                            ps.setInt(1, prevMonthTickets);
                            ps.setDouble(2, prevMonthRevenue);
                            ps.setInt(3, currentMonthTickets); // Set current month sales to recalculated value
                            ps.setDouble(4, currentMonthRevenue); // Set current month revenue to recalculated value
                            ps.setDate(5, java.sql.Date.valueOf(currentDate));
                            ps.setString(6, email);
                            ps.executeUpdate();
                            System.out.println("DAO resetMonthlySales: Updated DB for " + email);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO Reset Monthly Sales Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================
    // DELETE Operations
    // ==========================

    public static int deleteRowById(String connectionString, String tableName, String primaryKeyColumn, Object primaryKeyValue) {
        String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        System.out.println("DAO: Attempting to delete from " + tableName + " where " + primaryKeyColumn + " = " + primaryKeyValue);
        return update(connectionString, sql, primaryKeyValue);
    }

    // NEW: Delete seat selection for a user
    public static int deleteSeatSelection(String connectionString, int showtimeId, String userEmail) {
        String sql = "DELETE FROM seat_selections WHERE showtime_id = ? AND user_email = ?";
        return update(connectionString, sql, showtimeId, userEmail);
    }

    // REMOVED: deleteExpiredSeatSelections method is no longer needed for automatic timeout.
    // public static int deleteExpiredSeatSelections(String connectionString) {
    //     String sql = "DELETE FROM seat_selections WHERE selection_timestamp < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";
    //     return update(connectionString, sql);
    // }

    // NEW: Delete all seat selections for a showtime (cleanup)
    public static int deleteAllSeatSelections(String connectionString, int showtimeId) {
        String sql = "DELETE FROM seat_selections WHERE showtime_id = ?";
        return update(connectionString, sql, showtimeId);
    }

    // ==========================
    // Utility Methods
    // ==========================

    private static ResultSet select(String connectionString, String sql, Object... params) {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            // System.out.println("DAO Select: SQL = " + sql); // Commented out to reduce log spam
            // System.out.println("DAO Select: Params = " + java.util.Arrays.toString(params)); // Commented out
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.err.println("DAO Select Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static int update(String connectionString, String sql, Object... params) {
        try (
                Connection connection = DriverManager.getConnection(connectionString);
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            int rowsAffected = preparedStatement.executeUpdate();
            // System.out.println("DAO Update/Delete: SQL = " + sql); // Commented out to reduce log spam
            // System.out.println("DAO Update/Delete: Params = " + java.util.Arrays.toString(params)); // Commented out
            System.out.println("DAO Update/Delete: Rows Affected = " + rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            System.err.println("DAO Update/Delete Error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}