package com.joshiminh.wgcinema.data;

import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String sql = "SELECT * FROM movies WHERE title LIKE ? ORDER BY release_date LIMIT 20";
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
        return update(connectionString, sql, ticketsAdded, revenueAdded, email);
    }

    // NEW: Reset monthly sales and move current month to last month
    public static void resetMonthlySales(String connectionString) {
        try (Connection conn = DriverManager.getConnection(connectionString);
             Statement stmt = conn.createStatement()) {

            LocalDate currentDate = LocalDate.now();

            String fetchSql = "SELECT account_email, last_reset_date, total_tickets_sold_current_month, total_revenue_current_month FROM accounts";
            try (ResultSet rs = stmt.executeQuery(fetchSql)) {
                while (rs.next()) {
                    String email = rs.getString("account_email");
                    java.sql.Date lastResetDateSql = rs.getDate("last_reset_date");
                    LocalDate lastResetDate = lastResetDateSql != null ? lastResetDateSql.toLocalDate() : null;

                    // Check if reset is needed (if last reset was in a previous month or never set)
                    if (lastResetDate == null || lastResetDate.getMonth() != currentDate.getMonth() || lastResetDate.getYear() != currentDate.getYear()) {
                        int currentTickets = rs.getInt("total_tickets_sold_current_month");
                        double currentRevenue = rs.getDouble("total_revenue_current_month");

                        String updateSql = "UPDATE accounts SET total_tickets_sold_last_month = ?, total_revenue_last_month = ?, total_tickets_sold_current_month = 0, total_revenue_current_month = 0, last_reset_date = ? WHERE account_email = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                            ps.setInt(1, currentTickets);
                            ps.setDouble(2, currentRevenue);
                            ps.setDate(3, java.sql.Date.valueOf(currentDate));
                            ps.setString(4, email);
                            ps.executeUpdate();
                            System.out.println("Monthly sales reset for " + email);
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
