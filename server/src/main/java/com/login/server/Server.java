package com.login.server;

import com.login.server.db.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp Server chính.
 * - Mở ServerSocket lắng nghe kết nối từ client
 * - Dùng ThreadPool để xử lý nhiều client cùng lúc
 * - Mỗi client được xử lý bởi 1 ClientHandler trong Thread riêng
 */
public class Server {

    private final int port;
    private ServerSocket serverSocket;

    // ThreadPool: tối đa 50 client cùng lúc, tự scale down khi rảnh
    private final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    private volatile boolean running = false;

    public Server() {
        this.port = loadPort();
    }

    private int loadPort() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                                        .getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
                return Integer.parseInt(props.getProperty("server.port", "9999"));
            }
        } catch (Exception e) {
            System.err.println("[SERVER] Khong đoc đuoc port, dung mac đinh 9999");
        }
        return 9999;
    }

    /**
     * Khởi động server:
     * 1. Kiểm tra DB
     * 2. Mở ServerSocket
     * 3. Vòng lặp chấp nhận client
     */
    public void start() {
        System.out.println("=".repeat(50));
        System.out.println("   NETWORK LOGIN SYSTEM - SERVER");
        System.out.println("=".repeat(50));

        // Bước 1: Kiểm tra kết nối DB trước khi mở cổng
        if (!DBConnection.testConnection()) {
            System.err.println("[SERVER] Khong the ket noi DB → Server dung lai!");
            return;
        }

        // Bước 2: Mở ServerSocket
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[SERVER] Dang lang nghe tai port " + port + " ...");
            System.out.println("[SERVER] Nhan Ctrl+C de dung server.");
            System.out.println("-".repeat(50));

            // Bước 3: Vòng lặp chấp nhận client
            acceptClients();

        } catch (IOException e) {
            System.err.println("[SERVER] Khong the mo port " + port + ": " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Vòng lặp vô hạn chờ và chấp nhận kết nối client
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Giao cho ThreadPool xử lý, không block vòng lặp chính
                threadPool.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SERVER] Loi chap nhan ket noi: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Dừng server an toàn
     */
    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Loi dong server: " + e.getMessage());
        }
        System.out.println("[SERVER] Server dừng lai.");
    }
}
