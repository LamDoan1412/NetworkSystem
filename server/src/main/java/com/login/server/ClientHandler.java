package com.login.server;

import com.login.common.Message;
import com.login.server.db.UserDAO;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Xử lý request từ Client (chạy trên Thread riêng)
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private String clientIP;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerGUI gui;

    public ClientHandler(Socket socket, String clientIP, ServerGUI gui) {
        this.socket = socket;
        this.clientIP = clientIP;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            // Tạo stream (Output TRƯỚC, Input SAU)
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Nhận request từ Client
            Message request = (Message) inputStream.readObject();
            log("[REQUEST] Type: " + request.getType() + ", User: " + request.getUsername());

            // Xử lý request
            Message response = handleRequest(request);

            // Gửi response về Client
            outputStream.writeObject(response);
            outputStream.flush();

            log("[RESPONSE] Type: " + response.getType());

        } catch (Exception e) {
            log("[ERROR] " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                log("[DISCONNECT] Client ngat ket noi: " + clientIP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Xử lý request dựa trên Type
     */
    private Message handleRequest(Message request) {
        switch (request.getType()) {
            case LOGIN_REQUEST:
                return handleLoginRequest(request);

            case GET_SYSTEM_INFO_REQUEST:
                return handleSystemInfoRequest(request);

            case GET_LOGS_REQUEST:
                return handleLogsRequest(request);

            default:
                return Message.createLoginFailed("Invalid request type");
        }
    }

    /**
     * Xử lý đăng nhập (như cũ)
     */
    private Message handleLoginRequest(Message request) {
        return AuthService.authenticate(
                request.getUsername(),
                request.getPassword(),
                clientIP
        );
    }

    /**
     * Xử lý yêu cầu xem thông tin hệ thống
     */
    private Message handleSystemInfoRequest(Message request) {
        String username = request.getUsername();
        log("[REQUEST] " + username + " muốn xem thông tin hệ thống");

        // Nếu không có GUI → tự động approve
        if (gui == null) {
            log("[AUTO-APPROVED] Khong co GUI, tu dong chap nhan");
            String info = getSystemInfo(username, "Unknown");
            return Message.createRequestApproved(info);
        }

        // Hiện popup xin phép admin
        CompletableFuture<Boolean> approval = gui.showApprovalDialog(
                username,
                "xem thông tin đăng nhập"
        );

        try {
            // Chờ admin click (BLOCKING)
            boolean approved = approval.get();

            if (approved) {
                // Lấy role từ database
                String[] userData = com.login.server.db.UserDAO.findUser(username);
                String role = userData != null ? userData[1] : "Unknown";

                String info = getSystemInfo(username, role);
                return Message.createRequestApproved(info);
            } else {
                return Message.createRequestRejected(
                        "Server đã từ chối yêu cầu của bạn"
                );
            }

        } catch (Exception e) {
            log("[ERROR] Loi xu ly approval: " + e.getMessage());
            return Message.createRequestRejected("Loi xu ly: " + e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu xem lịch sử đăng nhập
     */
    private Message handleLogsRequest(Message request) {
        String username = request.getUsername();
        int limit = Integer.parseInt(request.getMessage());

        log("[REQUEST] " + username + " muốn xem lichj sử đăng nhập (limit=" + limit + ")");

        // Nếu không có GUI → tự động approve
        if (gui == null) {
            log("[AUTO-APPROVED] Khong co GUI, tu dong chap nhan");
            String logsData = getLoginHistoryData(limit);
            return Message.createRequestApproved(logsData);
        }

        // Hiện popup xin phép admin
        CompletableFuture<Boolean> approval = gui.showApprovalDialog(
                username,
                "xem lịch sử đăng nhập "
        );

        try {
            // Chờ admin click (BLOCKING)
            boolean approved = approval.get();

            if (approved) {
                String logsData = getLoginHistoryData(limit);
                return Message.createRequestApproved(logsData);
            } else {
                return Message.createRequestRejected(
                        "Server đã từ chối yêu cầu của bạn"
                );
            }

        } catch (Exception e) {
            log("[ERROR] Loi xu ly approval: " + e.getMessage());
            return Message.createRequestRejected("Loi xu ly: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin hệ thống (FORMAT NGẮN GỌN)
     */
    private String getSystemInfo(String username, String role) {
        String info =
                "Java version : " + System.getProperty("java.version") + "\n" +
                        "OS           : " + System.getProperty("os.name")      + "\n" +
                        "SERVER       : localhost:9999\n"                               +
                        "DATABASE     : SQL Server (HOLAD1412\\SQLEXPRESS)\n"           +
                        "NGƯỜI DÙNG   : " + username                           + "\n" +
                        "QUYỀN        : " + role                               + "\n" +
                        "THỜI GIAN    : " + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return info;
    }

    /**
     * Lấy dữ liệu lịch sử đăng nhập (format text đơn giản)
     */
    private String getLoginHistoryData(int limit) {
        try {
            List<String[]> logs = UserDAO.getLoginHistory(limit);

            StringBuilder data = new StringBuilder();
            data.append("========== LỊCH SỬ ĐĂNG NHẬP ==========\n\n");
            data.append(String.format("%-5s %-15s %-20s %-15s %-10s\n",
                    "STT", "USERNAME", "THỜI GIAN", "IP ADDRESS", "TRANG THAI"));
            data.append("-------------------------------------------------------------------\n");

            int index = 1;
            for (String[] log : logs) {
                data.append(String.format("%-5d %-15s %-20s %-15s %-10s\n",
                        index++,
                        log[0],  // username
                        log[1],  // time
                        log[2],  // ip
                        log[3]   // status
                ));
            }

            data.append("\nTong so ban ghi: ").append(logs.size()).append("\n");
            data.append("==========================================");

            return data.toString();

        } catch (Exception e) {
            log("[ERROR] Loi lay lich su: " + e.getMessage());
            return "Loi: " + e.getMessage();
        }
    }

    /**
     * Ghi log (console hoặc GUI)
     */
    private void log(String message) {
        if (gui != null) {
            gui.log(message);
        } else {
            System.out.println(message);
        }
    }
}