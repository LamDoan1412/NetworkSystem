package com.login.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối SQL Server.
 * Hỗ trợ cả Windows Authentication và SQL Server Authentication.
 *
 * Cấu hình trong config.properties:
 *   db.server=HOLAD1412\SQLEXPRESS
 *   db.name=LoginSystem
 *   db.windows.auth=true   → dùng Windows Auth (không cần user/pass)
 *   db.windows.auth=false  → dùng SQL Server Auth (cần user/pass)
 */
public class DBConnection {

    private static String url;
    private static String dbUsername;
    private static String dbPassword;
    private static boolean windowsAuth;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (is == null) {
                throw new RuntimeException("Khong tim thay config.properties!");
            }
            props.load(is);

            String server      = props.getProperty("db.server", "localhost\\SQLEXPRESS");
            String dbName      = props.getProperty("db.name", "LoginSystem");
            windowsAuth        = Boolean.parseBoolean(props.getProperty("db.windows.auth", "true"));
            dbUsername         = props.getProperty("db.username", "sa");
            dbPassword         = props.getProperty("db.password", "");

            if (windowsAuth) {
                // Windows Authentication - integratedSecurity=true
                // Can them sqljdbc_auth.dll vao thu muc, xem README
                url = String.format(
                    "jdbc:sqlserver://%s;databaseName=%s;integratedSecurity=true;trustServerCertificate=true;encrypt=false;",
                    server, dbName
                );
            } else {
                // SQL Server Authentication
                url = String.format(
                    "jdbc:sqlserver://%s;databaseName=%s;trustServerCertificate=true;encrypt=false;",
                    server, dbName
                );
            }

            System.out.println("[DB] URL: " + url);
            System.out.println("[DB] Auth: " + (windowsAuth ? "Windows Authentication" : "SQL Server Authentication"));

        } catch (IOException e) {
            throw new RuntimeException("Loi doc config.properties: " + e.getMessage());
        }
    }

    /**
     * Lấy kết nối mới tới SQL Server
     */
    public static Connection getConnection() throws SQLException {
        if (windowsAuth) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, dbUsername, dbPassword);
        }
    }

    /**
     * Kiểm tra kết nối khi server khởi động
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[DB] Ket noi SQL Server thanh cong!");
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] KHONG THE KET NOI SQL Server: " + e.getMessage());
            System.err.println("[DB] Hay kiem tra:");
            System.err.println("     1. SQL Server (SQLEXPRESS) dang chay chua?");
            System.err.println("        → Vao Services.msc tim 'SQL Server (SQLEXPRESS)'");
            System.err.println("     2. SQL Server Browser dang chay chua?");
            System.err.println("        → Vao Services.msc tim 'SQL Server Browser'");
            System.err.println("     3. TCP/IP da bat trong SQL Server Configuration Manager chua?");
            System.err.println("     4. Database 'LoginSystem' da tao chua?");
            if (windowsAuth) {
                System.err.println("     5. (Windows Auth) sqljdbc_auth.dll co trong PATH chua?");
                System.err.println("        → Hoac dung SQL Server Authentication de de hon");
            }
            return false;
        }
    }
}
