package com.joshiminh.wgcinema.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate; // Import LocalDate for date comparison

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
        // Updated to select regular_price and vip_price
        String sql = "SELECT *, regular_price, vip_price FROM showtimes WHERE showtime_id = ?";
        return select(connectionString, sql, showtimeId);
    }

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
        // Lấy email, tên và các cột doanh số mới từ bảng accounts
        String sql = """
           SELECT account_email, name, total_tickets_sold_current_month, total_revenue_current_month,
                  total_tickets_sold_last_month, total_revenue_last_month
           FROM accounts ORDER BY name
           """;
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

    public static int insertTransaction(String connectionString, int movieId, String totalPriceFormatted, String selectedSeats, int showroomId, String accountEmail, int showtimeId) {
        String sql = """
           INSERT INTO transactions (movie_id, amount, seats_preserved, showroom_id, account_email, showtime_id)
           VALUES (?, ?, ?, ?, ?, ?)
           """;
        // Sử dụng hàm tiện ích để chuyển đổi định dạng tiền tệ
        double amountValue = com.joshiminh.wgcinema.utils.AgentStyles.parseVietnameseCurrency(totalPriceFormatted);
        return update(
                connectionString,
                sql,
                movieId,
                amountValue, // Truyền giá trị double đã được parse
                selectedSeats,
                showroomId,
                accountEmail,
                showtimeId
        );
    }

    public static int insertTransactionReturnId(
            String connectionString,
            int movieId,
            String totalPriceFormatted,
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
            // Sử dụng hàm tiện ích để chuyển đổi định dạng tiền tệ
            double amountValue = com.joshiminh.wgcinema.utils.AgentStyles.parseVietnameseCurrency(totalPriceFormatted);
            ps.setInt(1, movieId);
            ps.setDouble(2, amountValue); // Set as double
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
        }
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

    /**
     * Updates the reserved chairs and chairs_booked for a showtime.
     * This method now includes concurrency control using database transactions and row-level locking.
     *
     * @param connectionString The database connection string.
     * @param reservedCount The number of chairs to add to reserved_chairs.
     * @param selectedSeats The comma-separated string of seats to book (e.g., "A1, B2").
     * @param showtimeId The ID of the showtime to update.
     * @return 1 if successful, -1 if seats are already booked (concurrency conflict), 0 if general database error.
     */
    public static int updateShowtimeSeats(String connectionString, int reservedCount, String selectedSeats, int showtimeId) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            connection.setAutoCommit(false); // Start transaction

            String currentChairsBooked = "";
            // Use SELECT FOR UPDATE for row-level locking (MySQL specific)
            // This prevents other transactions from reading/modifying this row until this transaction commits/rolls back.
            String selectSql = "SELECT chairs_booked FROM showtimes WHERE showtime_id = ? FOR UPDATE";
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setInt(1, showtimeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentChairsBooked = rs.getString("chairs_booked");
                } else {
                    connection.rollback(); // No showtime found, rollback
                    return 0; // Indicate failure
                }
                rs.close();
            }

            // Convert selectedSeats (comma-separated) to space-separated for internal check and concatenation
            String selectedSeatsSpaceSeparated = selectedSeats.replace(", ", " ");

            // Check for conflicts using the fetched (and locked) currentChairsBooked
            if (DAO.checkBooked(currentChairsBooked, selectedSeatsSpaceSeparated)) {
                connection.rollback(); // Conflict detected, rollback transaction
                return -1; // Special return value for "already booked"
            }

            // If no conflict, proceed with the update
            String updateSql = """
               UPDATE showtimes
               SET reserved_chairs = reserved_chairs + ?, chairs_booked = CONCAT(chairs_booked, ?)
               WHERE showtime_id = ?
               """;
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setInt(1, reservedCount);
                ps.setString(2, " " + selectedSeatsSpaceSeparated); // Add leading space for concatenation
                ps.setInt(3, showtimeId);
                int rowsAffected = ps.executeUpdate();
                connection.commit(); // Commit transaction
                return rowsAffected;
            }
        } catch (SQLException e) {
            System.err.println("DAO Update Showtime Seats Error: " + e.getMessage());
            e.printStackTrace();
            return 0; // Indicate general error
        }
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

    /**
     * Updates the total tickets sold and total revenue for an employee for the current month.
     *
     * @param connectionString The database connection string.
     * @param employeeEmail The email of the employee.
     * @param ticketsSold The number of tickets to add.
     * @param revenue The revenue amount to add.
     * @return The number of rows affected.
     */
    public static int updateEmployeeSales(String connectionString, String employeeEmail, int ticketsSold, double revenue) {
        String sql = """
           UPDATE accounts
           SET total_tickets_sold_current_month = total_tickets_sold_current_month + ?,
               total_revenue_current_month = total_revenue_current_month + ?
           WHERE account_email = ?
           """;
        return update(connectionString, sql, ticketsSold, revenue, employeeEmail);
    }

    /**
     * Resets monthly sales data for all employees if a new month has started.
     * Moves current month's sales to last month's sales and resets current month's sales to zero.
     * Updates the last_reset_date to the current date.
     * This method should be called periodically, e.g., at application startup or before displaying sales summary.
     *
     * @param connectionString The database connection string.
     */
    public static void resetMonthlySales(String connectionString) {
        String fetchSql = "SELECT account_email, total_tickets_sold_current_month, total_revenue_current_month, last_reset_date FROM accounts";
        String updateSql = """
           UPDATE accounts
           SET total_tickets_sold_last_month = ?,
               total_revenue_last_month = ?,
               total_tickets_sold_current_month = 0,
               total_revenue_current_month = 0,
               last_reset_date = CURRENT_DATE()
           WHERE account_email = ?
           """;

        try (Connection conn = DriverManager.getConnection(connectionString);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fetchSql)) {

            LocalDate currentDate = LocalDate.now();

            while (rs.next()) {
                String email = rs.getString("account_email");
                int currentTickets = rs.getInt("total_tickets_sold_current_month");
                double currentRevenue = rs.getDouble("total_revenue_current_month");
                java.sql.Date lastResetSqlDate = rs.getDate("last_reset_date");
                LocalDate lastResetDate = (lastResetSqlDate != null) ? lastResetSqlDate.toLocalDate() : null;

                // Check if reset is needed (if last reset was in a previous month or year)
                if (lastResetDate == null || lastResetDate.getMonth() != currentDate.getMonth() || lastResetDate.getYear() != currentDate.getYear()) {
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setInt(1, currentTickets);
                        ps.setDouble(2, currentRevenue);
                        ps.setString(3, email);
                        ps.executeUpdate();
                        System.out.println("Monthly sales reset for employee: " + email);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during monthly sales reset: " + e.getMessage());
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

    // ==========================
    // Utility Methods
    // ==========================

    /**
     * Checks if any of the selected seats are already present in the chairsBooked string.
     *
     * @param chairsBooked A space-separated string of already booked chairs (e.g., "A1 A2 B3").
     * @param selectedSeats A comma-separated string of seats to check (e.g., "A2, C5").
     * @return true if any selected seat is already booked, false otherwise.
     */
    public static boolean checkBooked(String chairsBooked, String selectedSeats) {
        if (chairsBooked == null || chairsBooked.trim().isEmpty()) {
            return false; // No chairs booked yet, so no conflict
        }
        if (selectedSeats == null || selectedSeats.trim().isEmpty()) {
            return false; // No seats selected to check
        }

        String[] bookedSeats = chairsBooked.trim().split("\\s+"); // Split by space
        String[] selectedSeatArray = selectedSeats.trim().split(", "); // Split by comma and space

        for (String selectedSeat : selectedSeatArray) {
            for (String bookedSeat : bookedSeats) {
                if (selectedSeat.equalsIgnoreCase(bookedSeat)) { // Case-insensitive comparison
                    return true; // Found a conflict
                }
            }
        }
        return false; // No conflict found
    }

    private static ResultSet select(String connectionString, String sql, Object... params) {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
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
            System.out.println("DAO Update/Delete: SQL = " + sql);
            System.out.println("DAO Update/Delete: Params = " + java.util.Arrays.toString(params));
            System.out.println("DAO Update/Delete: Rows Affected = " + rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            System.err.println("DAO Update/Delete Error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
