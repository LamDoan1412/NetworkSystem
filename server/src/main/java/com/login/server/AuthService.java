package com.login.server;

import com.login.common.Message;
import com.login.common.PasswordUtil;
import com.login.server.db.UserDAO;

/**
 * Business Logic cho việc xác thực đăng nhập.
 *
 * Quy trình:
 *  1. Kiểm tra username tồn tại
 *  2. Kiểm tra tài khoản có bị khóa không
 *  3. Kiểm tra brute-force (quá nhiều lần thất bại)
 *  4. Kiểm tra mật khẩu bằng BCrypt
 *  5. Ghi log kết quả
 */
public class AuthService {

    // Số lần thất bại tối đa trong 10 phút trước khi cảnh báo
    private static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * Xác thực đăng nhập
     * @param username tên đăng nhập
     * @param password mật khẩu plain text
     * @param clientIP địa chỉ IP của client (để ghi log)
     * @return Message phản hồi gửi về cho client
     */
    public static Message authenticate(String username, String password, String clientIP) {

        // ── Bước 1: Validate input ──
        if (username == null || username.trim().isEmpty()) {
            return Message.createLoginFailed("Username không được để trống!");
        }
        if (password == null || password.trim().isEmpty()) {
            return Message.createLoginFailed("Mật khẩu không được để trống!");
        }

        username = username.trim();

        // ── Bước 2: Kiểm tra brute-force ──
        int failedCount = UserDAO.countRecentFailedLogins(username);
        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            System.out.printf("[AUTH] Brute-force detected: %s từ %s (%d lần)%n",
                    username, clientIP, failedCount);
            UserDAO.insertLoginLog(username, clientIP, "BLOCKED");
            return Message.createLoginFailed(
                    "Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau 10 phút."
            );
        }

        // ── Bước 3: Tìm user trong DB ──
        String[] userData = UserDAO.findUser(username);

        if (userData == null) {
            // Username không tồn tại - ghi log và báo lỗi chung chung (bảo mật hơn)
            UserDAO.insertLoginLog(username, clientIP, "FAILED");
            System.out.printf("[AUTH] FAILED: Username '%s' không tồn tại (IP: %s)%n",
                    username, clientIP);
            return Message.createLoginFailed("Username hoặc mật khẩu không đúng!");
        }

        String storedHash = userData[0];
        String role       = userData[1];
        boolean isActive  = "1".equals(userData[2]) || "true".equalsIgnoreCase(userData[2]);

        // ── Bước 4: Kiểm tra tài khoản có bị khóa không ──
        if (!isActive) {
            UserDAO.insertLoginLog(username, clientIP, "LOCKED");
            System.out.printf("[AUTH] LOCKED: Tài khoản '%s' bị khóa (IP: %s)%n",
                    username, clientIP);

            // ✅ ĐÃ SỬA: THÊM THAM SỐ
            return Message.createLoginLocked("Tài khoản đã bị khóa. Vui lòng liên hệ admin để mở khóa.");
        }

        // ── Bước 5: Kiểm tra mật khẩu bằng BCrypt ──
        boolean passwordMatch = PasswordUtil.verify(password, storedHash);

        if (!passwordMatch) {
            UserDAO.insertLoginLog(username, clientIP, "FAILED");
            System.out.printf("[AUTH] FAILED: Sai mật khẩu cho '%s' (IP: %s)%n",
                    username, clientIP);
            return Message.createLoginFailed("Username hoặc mật khẩu không đúng!");
        }

        // ── Bước 6: Đăng nhập thành công ──
        UserDAO.insertLoginLog(username, clientIP, "SUCCESS");
        System.out.printf("[AUTH] SUCCESS: '%s' [%s] từ %s%n", username, role, clientIP);
        return Message.createLoginSuccess(username, role);
    }
}