package com.login.common;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa mật khẩu sử dụng BCrypt.
 * BCrypt tự động thêm salt ngẫu nhiên, rất an toàn.
 *
 * Cách dùng:
 *   String hash = PasswordUtil.hash("myPassword");   // Khi đăng ký
 *   boolean ok  = PasswordUtil.verify("myPassword", hash); // Khi đăng nhập
 */
public class PasswordUtil {

    // Work factor: 12 là mức cân bằng giữa bảo mật và tốc độ
    private static final int WORK_FACTOR = 12;

    /**
     * Hash mật khẩu plain text → chuỗi BCrypt (60 ký tự)
     * Dùng khi: tạo tài khoản, đổi mật khẩu
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * So sánh mật khẩu nhập vào với hash trong DB
     * Dùng khi: xác thực đăng nhập
     *
     * @return true nếu khớp, false nếu sai
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Hash không hợp lệ
            return false;
        }
    }
}
