package com.login.common;

import java.io.Serializable;

/**
 * Object dùng để truyền dữ liệu giữa Client và Server qua TCP Socket.
 * Implements Serializable để có thể dùng ObjectInputStream/ObjectOutputStream.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    // Loại message
    public enum Type {
        LOGIN_REQUEST,   // Client gửi yêu cầu đăng nhập
        LOGIN_SUCCESS,   // Server phản hồi: đăng nhập thành công
        LOGIN_FAILED,    // Server phản hồi: sai username/password
        LOGIN_LOCKED,    // Server phản hồi: tài khoản bị khóa
        LOGOUT           // Client gửi yêu cầu đăng xuất
    }

    private Type type;
    private String username;
    private String password;  // plain text từ client, server sẽ hash khi kiểm tra
    private String message;   // thông báo từ server
    private String role;      // vai trò user (admin/user)
    private String timestamp; // thời gian

    // ==================== Constructor ====================

    public Message() {}

    public Message(Type type) {
        this.type = type;
    }

    public Message(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Tạo message đăng nhập từ phía Client
     */
    public static Message createLoginRequest(String username, String password) {
        Message msg = new Message(Type.LOGIN_REQUEST);
        msg.username = username;
        msg.password = password;
        return msg;
    }

    /**
     * Tạo message phản hồi thành công từ phía Server
     */
    public static Message createLoginSuccess(String username, String role) {
        Message msg = new Message(Type.LOGIN_SUCCESS);
        msg.username = username;
        msg.role = role;
        msg.message = "Đăng nhập thành công! Xin chào " + username + " [" + role + "]";
        return msg;
    }

    /**
     * Tạo message phản hồi thất bại từ phía Server
     */
    public static Message createLoginFailed(String reason) {
        Message msg = new Message(Type.LOGIN_FAILED);
        msg.message = reason;
        return msg;
    }

    /**
     * Tạo message tài khoản bị khóa từ phía Server
     */
    public static Message createLoginLocked() {
        Message msg = new Message(Type.LOGIN_LOCKED);
        msg.message = "Tài khoản bị khóa. Vui lòng liên hệ Admin.";
        return msg;
    }

    // ==================== Getters & Setters ====================

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Message{type=" + type + ", username=" + username + ", message=" + message + "}";
    }
}
