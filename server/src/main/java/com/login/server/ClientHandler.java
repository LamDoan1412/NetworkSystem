package com.login.server;

import com.login.common.Message;
import com.login.server.db.UserDAO;
import java.io.*;
import java.net.Socket;
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
        log("[REQUEST] " + username + " muon xem thong tin he thong");

        // Nếu không có GUI → tự động approve
        if (gui == null) {
            log("[AUTO-APPROVED] Khong co GUI, tu dong chap nhan");
            String info = getSystemInfo();
            return Message.createRequestApproved(info);
        }

        // Hiện popup xin phép admin
        CompletableFuture<Boolean> approval = gui.showApprovalDialog(
                username,
                "xem thong tin he thong"
        );

        try {
            // Chờ admin click (BLOCKING)
            boolean approved = approval.get();

            if (approved) {
                String info = getSystemInfo();
                return Message.createRequestApproved(info);
            } else {
                return Message.createRequestRejected(
                        "Admin da tu choi yeu cau cua ban"
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

        log("[REQUEST] " + username + " muon xem lich su dang nhap (limit=" + limit + ")");

        // Nếu không có GUI → tự động approve
        if (gui == null) {
            log("[AUTO-APPROVED] Khong co GUI, tu dong chap nhan");
            String logsData = getLoginHistoryData(limit);
            return Message.createRequestApproved(logsData);
        }

        // Hiện popup xin phép admin
        CompletableFuture<Boolean> approval = gui.showApprovalDialog(
                username,
                "xem lich su dang nhap (top " + limit + ")"
        );

        try {
            // Chờ admin click (BLOCKING)
            boolean approved = approval.get();

            if (approved) {
                String logsData = getLoginHistoryData(limit);
                return Message.createRequestApproved(logsData);
            } else {
                return Message.createRequestRejected(
                        "Admin da tu choi yeu cau cua ban"
                );
            }

        } catch (Exception e) {
            log("[ERROR] Loi xu ly approval: " + e.getMessage());
            return Message.createRequestRejected("Loi xu ly: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin hệ thống
     */
    private String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("========== THONG TIN HE THONG ==========\n\n");

        // Java info
        info.append("[JAVA]\n");
        info.append("  Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("  Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("  Home: ").append(System.getProperty("java.home")).append("\n\n");

        // OS info
        info.append("[OPERATING SYSTEM]\n");
        info.append("  Name: ").append(System.getProperty("os.name")).append("\n");
        info.append("  Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("  Architecture: ").append(System.getProperty("os.arch")).append("\n\n");

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        info.append("[MEMORY]\n");
        info.append("  Max Memory: ").append(maxMemory).append(" MB\n");
        info.append("  Total Memory: ").append(totalMemory).append(" MB\n");
        info.append("  Free Memory: ").append(freeMemory).append(" MB\n");
        info.append("  Used Memory: ").append(usedMemory).append(" MB\n");
        info.append("  Usage: ").append((usedMemory * 100 / maxMemory)).append("%\n\n");

        // CPU info
        info.append("[CPU]\n");
        info.append("  Available Processors: ").append(runtime.availableProcessors()).append("\n\n");

        // User info
        info.append("[USER]\n");
        info.append("  User Name: ").append(System.getProperty("user.name")).append("\n");
        info.append("  User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("  User Dir: ").append(System.getProperty("user.dir")).append("\n\n");

        info.append("==========================================");

        return info.toString();
    }

    /**
     * Lấy dữ liệu lịch sử đăng nhập (format text đơn giản)
     */
    private String getLoginHistoryData(int limit) {
        try {
            List<String[]> logs = UserDAO.getLoginHistory(limit);

            StringBuilder data = new StringBuilder();
            data.append("========== LICH SU DANG NHAP ==========\n\n");
            data.append(String.format("%-5s %-15s %-20s %-15s %-10s\n",
                    "STT", "USERNAME", "THOI GIAN", "IP ADDRESS", "TRANG THAI"));
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