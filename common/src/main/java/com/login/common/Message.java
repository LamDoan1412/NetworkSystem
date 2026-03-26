package com.login.common;

import java.io.Serializable;

/**
 * Lớp Message đại diện cho giao thức truyền tin giữa Client và Server.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum định nghĩa các loại Message
     */
    public enum Type {
        LOGIN_REQUEST,              // Client → Server: Yêu cầu đăng nhập
        LOGIN_SUCCESS,              // Server → Client: Đăng nhập thành công
        LOGIN_FAILED,               // Server → Client: Đăng nhập thất bại
        LOGIN_LOCKED,               // Server → Client: Tài khoản bị khóa
        LOGOUT,                     // Client → Server: Đăng xuất

        // ===== THÊM MỚI - Approval System =====
        GET_SYSTEM_INFO_REQUEST,    // Client → Server: Yêu cầu xem thông tin hệ thống
        GET_LOGS_REQUEST,           // Client → Server: Yêu cầu xem lịch sử đăng nhập

        REQUEST_APPROVED,           // Server → Client: Yêu cầu đã được chấp nhận
        REQUEST_REJECTED            // Server → Client: Yêu cầu bị từ chối
    }

    // Các thuộc tính
    private Type type;
    private String username;
    private String password;
    private String message;
    private String role;

    // Constructor private
    private Message(Type type) {
        this.type = type;
    }

    // ===== FACTORY METHODS - Login/Logout =====

    public static Message createLoginRequest(String username, String password) {
        Message msg = new Message(Type.LOGIN_REQUEST);
        msg.username = username;
        msg.password = password;
        return msg;
    }

    public static Message createLoginSuccess(String username, String role) {
        Message msg = new Message(Type.LOGIN_SUCCESS);
        msg.username = username;
        msg.role = role;
        msg.message = "Dang nhap thanh cong! Xin chao " + username + " [" + role + "]";
        return msg;
    }

    public static Message createLoginFailed(String errorMessage) {
        Message msg = new Message(Type.LOGIN_FAILED);
        msg.message = errorMessage;
        return msg;
    }

    public static Message createLoginLocked(String lockMessage) {
        Message msg = new Message(Type.LOGIN_LOCKED);
        msg.message = lockMessage;
        return msg;
    }

    // ===== FACTORY METHODS - Request System =====

    public static Message createGetSystemInfoRequest(String username) {
        Message msg = new Message(Type.GET_SYSTEM_INFO_REQUEST);
        msg.username = username;
        return msg;
    }

    public static Message createGetLogsRequest(String username, int limit) {
        Message msg = new Message(Type.GET_LOGS_REQUEST);
        msg.username = username;
        msg.message = String.valueOf(limit);
        return msg;
    }

    public static Message createRequestApproved(String data) {
        Message msg = new Message(Type.REQUEST_APPROVED);
        msg.message = data;
        return msg;
    }

    public static Message createRequestRejected(String reason) {
        Message msg = new Message(Type.REQUEST_REJECTED);
        msg.message = reason;
        return msg;
    }

    // ===== GETTERS =====

    public Type getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", username=" + username + ", message=" + message + "}";
    }
}