package com.login.client;

import com.login.common.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**
 * Lớp Client quản lý kết nối TCP tới Server.
 * Đóng gói toàn bộ logic giao tiếp socket.
 *
 * Cách dùng:
 *   Client client = new Client();
 *   if (client.connect()) {
 *       Message result = client.login("admin", "Admin@123");
 *       client.disconnect();
 *   }
 */
public class Client {

    private String host;
    private int port;
    private int timeout;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private boolean connected = false;

    public Client() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                                        .getResourceAsStream("client.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Không đọc được client.properties, dùng mặc định.");
        }

        this.host    = props.getProperty("server.host", "localhost");
        this.port    = Integer.parseInt(props.getProperty("server.port", "9999"));
        this.timeout = Integer.parseInt(props.getProperty("connection.timeout", "5000"));
    }

    /**
     * Kết nối tới server
     * @return true nếu kết nối thành công
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);

            // QUAN TRỌNG: Output TRƯỚC Input (tránh deadlock)
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream  = new ObjectInputStream(socket.getInputStream());

            connected = true;
            System.out.println("[CLIENT] Ket noi thanh cong toi " + host + ":" + port);
            return true;

        } catch (SocketTimeoutException e) {
            System.err.println("[CLIENT] Timeout: Server khong phan hoi.");
        } catch (IOException e) {
            System.err.println("[CLIENT] Khong the ket noi server: " + e.getMessage());
            System.err.println("[CLIENT] Hay kiem tra server dang chay chua?");
        }
        return false;
    }

    /**
     * Gửi yêu cầu đăng nhập
     * @return Message phản hồi từ server
     */
    public Message login(String username, String password) {
        if (!connected) {
            return Message.createLoginFailed("Chua ket noi toi server!");
        }

        try {
            // Gửi request
            Message request = Message.createLoginRequest(username, password);
            outputStream.writeObject(request);
            outputStream.flush();

            // Đợi phản hồi
            Message response = (Message) inputStream.readObject();
            return response;

        } catch (SocketTimeoutException e) {
            return Message.createLoginFailed("Server khong phan hoi (timeout).");
        } catch (IOException | ClassNotFoundException e) {
            connected = false;
            return Message.createLoginFailed("Loi ket noi: " + e.getMessage());
        }
    }

    /**
     * Gửi yêu cầu đăng xuất và đóng kết nối
     */
    public void disconnect() {
        if (!connected) return;

        try {
            // Gửi thông báo logout trước khi đóng
            Message logout = new Message(Message.Type.LOGOUT);
            outputStream.writeObject(logout);
            outputStream.flush();
        } catch (IOException ignored) {}

        closeConnection();
    }

    private void closeConnection() {
        connected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[CLIENT] Da ngat ket noi.");
        } catch (IOException e) {
            System.err.println("[CLIENT] Loi dong ket noi: " + e.getMessage());
        }
    }

    public boolean isConnected() { return connected; }
    public String getHost() { return host; }
    public int getPort() { return port; }
}
