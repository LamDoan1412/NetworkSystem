package com.login.server;

import com.login.server.db.DBConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server lắng nghe kết nối từ Client
 */
public class Server {

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    private ServerGUI gui; // Giao diện server (có thể null)

    // Constructor cho console mode (không có GUI)
    public Server() {
        this.gui = null;
    }

    // Constructor cho GUI mode
    public Server(ServerGUI gui) {
        this.gui = gui;
    }

    public void start() {
        try {
            // Test kết nối database
            if (!DBConnection.testConnection()) {
                log("[ERROR] Khong the ket noi database!");
                return;
            }

            serverSocket = new ServerSocket(9999);
            threadPool = Executors.newFixedThreadPool(50);
            running = true;

            log("[SERVER] Dang lang nghe tai port 9999...");
            log("[SERVER] ThreadPool: 50 threads");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();

                log("[CONNECT] Client ket noi: " + clientIP);

                // Truyền GUI vào ClientHandler
                threadPool.execute(new ClientHandler(clientSocket, clientIP, gui));
            }

        } catch (Exception e) {
            if (running) {
                log("[ERROR] " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown();
            }
            log("[SERVER] Da dung thanh cong");
        } catch (Exception e) {
            log("[ERROR] Loi khi dung server: " + e.getMessage());
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