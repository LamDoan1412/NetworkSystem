package com.login.server.db;

import java.sql.*;

/**
 * Data Access Object cho SQL Server.
 * Dùng T-SQL thay vì MySQL syntax.
 */
public class UserDAO {

    // ==================== User Operations ====================

    /**
     * Tìm user theo username
     * @return [password_hash, role, is_active] hoặc null nếu không tìm thấy
     */
    public static String[] findUser(String username) {
        String sql = "SELECT password, role, is_active FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new String[]{
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getBoolean("is_active") ? "1" : "0"
                };
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Loi truy van user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tạo user mới
     */
    public static boolean createUser(String username, String passwordHash, String role) {
        String sql = "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, ?, 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Loi tao user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    public static boolean userExists(String username) {
        // SQL Server dùng TOP thay vì LIMIT
        String sql = "SELECT TOP 1 id FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("[DAO] Loi kiem tra user: " + e.getMessage());
            return false;
        }
    }

    // ==================== Login Log Operations ====================

    /**
     * Ghi log đăng nhập
     * SQL Server dùng GETDATE() thay vì NOW()
     */
    public static void insertLoginLog(String username, String ipAddress, String status) {
        String sql = "INSERT INTO login_logs (username, login_time, ip_address, status) VALUES (?, GETDATE(), ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, ipAddress);
            stmt.setString(3, status);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DAO] Loi ghi log: " + e.getMessage());
        }
    }

    /**
     * Đếm số lần đăng nhập thất bại trong 10 phút gần đây.
     * SQL Server dùng DATEADD thay vì DATE_SUB
     */
    public static int countRecentFailedLogins(String username) {
        String sql = "SELECT COUNT(*) FROM login_logs " +
                     "WHERE username = ? AND status = 'FAILED' " +
                     "AND login_time > DATEADD(MINUTE, -10, GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[DAO] Loi dem failed logins: " + e.getMessage());
        }
        return 0;
    }

    // ==================== User Management (Admin) ====================

    /**
     * Lấy danh sách tất cả user (cho Admin)
     * @return List<String[]> - mỗi phần tử là [username, role, is_active, created_at]
     */
    public static java.util.List<String[]> getAllUsers() {
        java.util.List<String[]> users = new java.util.ArrayList<>();
        String sql = "SELECT username, role, is_active, created_at FROM users ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new String[]{
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getBoolean("is_active") ? "1" : "0",
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Loi lay danh sach user: " + e.getMessage());
        }
        return users;
    }

    /**
     * Khóa/Mở khóa tài khoản
     */
    public static boolean toggleUserStatus(String username) {
        String sql = "UPDATE users SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Loi toggle user status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa user
     */
    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Loi xoa user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đổi mật khẩu
     */
    public static boolean changePassword(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password = ?, updated_at = GETDATE() WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Loi doi mat khau: " + e.getMessage());
            return false;
        }
    }

    // ==================== Login History ====================

    /**
     * Lấy lịch sử đăng nhập (giới hạn số lượng)
     * @param limit số bản ghi muốn lấy (ví dụ: 50)
     * @return List<String[]> - [username, login_time, ip_address, status]
     */
    public static java.util.List<String[]> getLoginHistory(int limit) {
        java.util.List<String[]> logs = new java.util.ArrayList<>();
        String sql = "SELECT TOP " + limit + " username, login_time, ip_address, status " +
                     "FROM login_logs ORDER BY login_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(new String[]{
                    rs.getString("username"),
                    rs.getString("login_time"),
                    rs.getString("ip_address"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Loi lay lich su dang nhap: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Lấy lịch sử đăng nhập của 1 user cụ thể
     */
    public static java.util.List<String[]> getLoginHistoryByUser(String username, int limit) {
        java.util.List<String[]> logs = new java.util.ArrayList<>();
        String sql = "SELECT TOP " + limit + " login_time, ip_address, status " +
                     "FROM login_logs WHERE username = ? ORDER BY login_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(new String[]{
                    username,
                    rs.getString("login_time"),
                    rs.getString("ip_address"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Loi lay lich su user: " + e.getMessage());
        }
        return logs;
    }
}
