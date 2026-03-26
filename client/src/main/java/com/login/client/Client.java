package com.login.client;

import com.login.common.Message;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

/**
 * Client kết nối tới Server
 */
public class Client {

    private String serverHost;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String currentUsername; // Lưu username sau khi login thành công

    public Client() {
        loadConfig();
    }

    /**
     * Đọc cấu hình từ client.properties
     */
    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("client.properties")) {

            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                serverHost = prop.getProperty("server.host", "localhost");
                serverPort = Integer.parseInt(prop.getProperty("server.port", "9999"));
            } else {
                serverHost = "localhost";
                serverPort = 9999;
            }

        } catch (Exception e) {
            serverHost = "localhost";
            serverPort = 9999;
        }
    }

    /**
     * Kết nối tới Server
     */
    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đăng nhập
     */
    public Message login(String username, String password) {
        try {
            Message request = Message.createLoginRequest(username, password);
            outputStream.writeObject(request);
            outputStream.flush();

            Message response = (Message) inputStream.readObject();

            // Lưu username nếu đăng nhập thành công
            if (response.getType() == Message.Type.LOGIN_SUCCESS) {
                this.currentUsername = username;
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Message.createLoginFailed("Loi ket noi: " + e.getMessage());
        }
    }

    /**
     * YÊU CẦU xem thông tin hệ thống
     * (Cần approval từ admin server)
     */
    public Message requestSystemInfo() {
        Socket tempSocket = null;
        try {
            // Tạo kết nối mới
            tempSocket = new Socket(serverHost, serverPort);
            ObjectOutputStream out = new ObjectOutputStream(tempSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(tempSocket.getInputStream());

            // Gửi request
            Message request = Message.createGetSystemInfoRequest(currentUsername);
            out.writeObject(request);
            out.flush();

            // Chờ response (admin phải approve)
            Message response = (Message) in.readObject();

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Message.createRequestRejected("Loi ket noi: " + e.getMessage());
        } finally {
            try {
                if (tempSocket != null) tempSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * YÊU CẦU xem lịch sử đăng nhập
     * (Cần approval từ admin server)
     */
    public Message requestLoginHistory(int limit) {
        Socket tempSocket = null;
        try {
            // Tạo kết nối mới
            tempSocket = new Socket(serverHost, serverPort);
            ObjectOutputStream out = new ObjectOutputStream(tempSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(tempSocket.getInputStream());

            // Gửi request
            Message request = Message.createGetLogsRequest(currentUsername, limit);
            out.writeObject(request);
            out.flush();

            // Chờ response (admin phải approve)
            Message response = (Message) in.readObject();

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Message.createRequestRejected("Loi ket noi: " + e.getMessage());
        } finally {
            try {
                if (tempSocket != null) tempSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }
}