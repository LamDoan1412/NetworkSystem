package com.login.server;

import com.login.common.Message;

import java.io.*;
import java.net.Socket;

/**
 * Thread xử lý từng client kết nối vào server.
 * Mỗi client có 1 ClientHandler riêng → xử lý song song (đa luồng).
 *
 * Luồng hoạt động:
 *  Client kết nối → Server tạo ClientHandler → đọc Message → xác thực → phản hồi → đóng
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final String clientIP;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientIP = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        System.out.println("[SERVER] Client ket noi: " + clientIP);

        try {
            // QUAN TRỌNG: Tạo ObjectOutputStream TRƯỚC ObjectInputStream
            // Nếu làm ngược lại sẽ bị deadlock!
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream  = new ObjectInputStream(clientSocket.getInputStream());

            // Xử lý các request từ client (có thể nhiều lần trong 1 kết nối)
            processRequests();

        } catch (EOFException e) {
            // Client đóng kết nối bình thường
            System.out.println("[SERVER] Client " + clientIP + " da ngat ket noi.");
        } catch (IOException e) {
            System.err.println("[SERVER] Loi ket noi voi " + clientIP + ": " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Vòng lặp đọc và xử lý request từ client
     */
    private void processRequests() throws IOException {
        while (!clientSocket.isClosed()) {
            try {
                // Đọc Message từ client
                Message request = (Message) inputStream.readObject();
                System.out.println("[SERVER] Nhận từ " + clientIP + ": " + request);

                Message response = handleMessage(request);

                // Gửi phản hồi về client
                outputStream.writeObject(response);
                outputStream.flush();

                // Nếu client logout thì thoát vòng lặp
                if (request.getType() == Message.Type.LOGOUT) {
                    break;
                }

            } catch (ClassNotFoundException e) {
                System.err.println("[SERVER] Lỗi deserialize message: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * Xử lý từng loại message
     */
    private Message handleMessage(Message request) {
        if (request.getType() == null) {
            return Message.createLoginFailed("Message không hợp lệ!");
        }

        switch (request.getType()) {
            case LOGIN_REQUEST:
                // Gọi AuthService để xác thực
                return AuthService.authenticate(
                    request.getUsername(),
                    request.getPassword(),
                    clientIP
                );

            case LOGOUT:
                System.out.println("[SERVER] " + clientIP + " đã đăng xuất.");
                return new Message(Message.Type.LOGOUT, "Đã đăng xuất thành công.");

            default:
                return Message.createLoginFailed("Loại request không được hỗ trợ.");
        }
    }

    /**
     * Đóng socket và các stream an toàn
     */
    private void closeConnection() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            System.out.println("[SERVER] Đã đóng kết nối: " + clientIP);
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi đóng kết nối: " + e.getMessage());
        }
    }
}
