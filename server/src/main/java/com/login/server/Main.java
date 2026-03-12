package com.login.server;

import com.login.common.PasswordUtil;
import com.login.server.db.UserDAO;

/**
 * Entry point của Server.
 * Có thể seed dữ liệu test trước khi khởi động.
 */
public class Main {

    public static void main(String[] args) {
        // Seed dữ liệu test nếu chưa có (chạy lần đầu)
        seedTestData();

        // Khởi động server
        Server server = new Server();

        // Hook để dừng server khi Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVER] Đang tắt server...");
            server.stop();
        }));

        server.start();
    }


    private static void seedTestData() {
        System.out.println("[SEED] Kiểm tra dữ liệu test...");

        // Admin account
        if (!UserDAO.userExists("admin")) {
            String hash = PasswordUtil.hash("admin123");
            UserDAO.createUser("admin", hash, "admin");
            System.out.println("[SEED] Tạo tài khoản admin / admin123");
        }

        // User account
        if (!UserDAO.userExists("admin1412")) {
            String hash = PasswordUtil.hash("1412");
            UserDAO.createUser("adim1412", hash, "user");
            System.out.println("[SEED] Tạo tài khoản admin1412 / 1412");
        }

        System.out.println("[SEED] Xong!");
    }
}
