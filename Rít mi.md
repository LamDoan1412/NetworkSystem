
# Network Login System — Tổng quan

Mô tả ngắn: Hệ thống gồm 3 thành phần chính (Client, Server, Database) giao tiếp qua TCP socket để thực hiện đăng nhập, xác thực bằng BCrypt và ghi lịch sử đăng nhập.

*** 1. Kiến trúc tổng thể ***

```
┌─────────────┐    TCP Socket     ┌─────────────┐    JDBC      ┌─────────────┐
│   CLIENT    │ ←────────────── → │   SERVER    │ ←──────────→ │  SQL Server │
│ (LoginForm) │   gửi/nhận Object │ (port 9999) │   truy vấn   │  (Database) │
└─────────────┘                   └─────────────┘              └─────────────┘
```

Thành phần:
- Client — giao diện người dùng (Swing) gửi `username`/`password` gói trong `Message`.
- Server — lắng nghe cổng 9999, xử lý bằng `ThreadPool`, thực hiện xác thực qua `AuthService`.
- Database — SQL Server lưu bảng `users` và `login_logs`.

*** 2. Luồng hoạt động khi đăng nhập (sequence) ***

1. Client nhập username/password trên `LoginForm`.
2. `Client.login()` đóng gói thành `Message` (type = LOGIN_REQUEST) → gửi qua socket.
3. Server nhận kết nối → `ClientHandler.handleMessage()` đọc `Message`.
4. `AuthService.authenticate()` thực hiện các bước:
   - Kiểm tra input rỗng.
   - Kiểm tra brute-force bằng `UserDAO.countRecentFailedLogins`.
   - Tìm user trong DB `UserDAO.findUser`.
   - Kiểm tra `is_active`.
   - So sánh mật khẩu bằng `PasswordUtil.verify` (BCrypt).
5. Ghi log kết quả vào `login_logs` bằng `UserDAO.insertLoginLog`.
6. Server trả `Message` (LOGIN_SUCCESS / LOGIN_FAILED / LOGIN_LOCKED) về Client.
7. Client hiển thị kết quả cho người dùng.

*** 3. Giải thích các file chính ***

**Module common** — dùng chung cho Client/Server
- `Message.java` — lớp Serializable dùng làm phong bì truyền dữ liệu.

Ví dụ cấu trúc `Message` (tóm tắt):

```java
public class Message implements Serializable {
    public enum Type { LOGIN_REQUEST, LOGIN_SUCCESS, LOGIN_FAILED, LOGIN_LOCKED, LOGOUT }
    private Type type;
    private String username;
    private String password; // chỉ gửi từ Client→Server
    private String message;  // thông báo hiển thị
    private String role;     // admin / user
}
```

Lý do dùng Object thay vì String: tránh parse thủ công, dễ mở rộng, an toàn hơn.

- `PasswordUtil.java` — mã hóa và kiểm tra BCrypt.
  - Khi tạo tài khoản: `String hash = PasswordUtil.hash("Admin@123");`
  - Khi đăng nhập: `PasswordUtil.verify(plain, hashFromDB)`.
  - Vì sao: bảo vệ mật khẩu nếu DB bị leak; BCrypt tự tạo salt.

**Module server**
- `DBConnection.java` — tạo `Connection` JDBC tới SQL Server (đọc từ `config.properties`).
- `UserDAO.java` — tất cả các truy vấn SQL: tìm user, tạo user, ghi `login_logs`, đếm failed trong 10 phút v.v.

Tách DAO để dễ thay engine DB (chỉ sửa DAO khi đổi DB).

`AuthService.java` — quy trình xác thực (tóm tắt 6 bước):

```
Bước 1: Kiểm tra username/password rỗng?
Bước 2: Kiểm tra số lần thất bại trong 10 phút (>5?) → chặn brute-force
Bước 3: Tìm user trong DB?
Bước 4: is_active == 0 ? → trả LOCKED
Bước 5: BCrypt.verify(passwordNhap, hashTrongDB) ?
Bước 6: Ghi log vào login_logs → trả Message kết quả
```

- `Server.java` — mở `ServerSocket` cổng 9999, nhận kết nối, dispatch `ClientHandler` bằng `ThreadPool`.

Ví dụ vòng lắng nghe:

```java
ServerSocket serverSocket = new ServerSocket(9999);
while (true) {
    Socket clientSocket = serverSocket.accept();
    threadPool.execute(new ClientHandler(clientSocket));
}
```

Lý do dùng `ThreadPool`: xử lý nhiều client đồng thời mà không tạo quá nhiều thread không kiểm soát.

- `ClientHandler.java` — mỗi kết nối một handler trên thread riêng:

```java
public void run() {
    outputStream = new ObjectOutputStream(socket.getOutputStream());
    inputStream  = new ObjectInputStream(socket.getInputStream());
    Message request = (Message) inputStream.readObject();
    Message response = handleMessage(request);
    outputStream.writeObject(response);
}
```

Quan trọng: tạo `ObjectOutputStream` **trước** `ObjectInputStream` để tránh deadlock Java stream header.

**Module client**
- `Client.java` — quản lý kết nối TCP, gửi `Message`, nhận `Message`.

```java
socket = new Socket("localhost", 9999);
outputStream = new ObjectOutputStream(socket.getOutputStream());
inputStream  = new ObjectInputStream(socket.getInputStream());

public Message login(String username, String password) {
    Message request = Message.createLoginRequest(username, password);
    outputStream.writeObject(request);
    return (Message) inputStream.readObject();
}
```

- `LoginForm.java` — Swing UI: tất cả các thao tác mạng phải chạy trên thread riêng, cập nhật UI trên Event Dispatch Thread (EDT).

Ví dụ khi bấm Đăng nhập:

```java
new Thread(() -> {
    Message res = client.login(user, pass);
    SwingUtilities.invokeLater(() -> handleResponse(res));
}).start();
```

Vì sao: gọi `client.login()` trực tiếp trên EDT sẽ làm treo giao diện khi chờ server.

*** 4. Cơ chế Socket hoạt động ***

Tóm tắt TCP handshake, accept(), và truyền `Message` bằng `ObjectOutputStream`/`ObjectInputStream`.

Server và Client thực hiện 3 bước bắt tay TCP, sau đó ghi/đọc Objects qua stream.

*** 5. Cơ sở dữ liệu (schema) ***

`users`:
- `id` (PK) | `username` | `password` (BCrypt) | `role` | `is_active`

`login_logs`:
- `id` (PK) | `username` | `login_time` | `ip_address` | `status` (SUCCESS / FAILED / LOCKED)

Ví dụ dữ liệu:

```
users:
  admin | $2a$12$xKm9... | admin | 1
  user1 | $2a$12$yNp3... | user  | 1

login_logs:
  admin | 2026-02-17 14:12:44 | 127.0.0.1 | SUCCESS
  admin | 2026-02-17 14:10:01 | 127.0.0.1 | FAILED
```

*** 6. Tóm tắt một câu ***

Client đóng gói `username`+`password` vào `Message`, gửi qua TCP đến Server; Server dùng `ThreadPool` và `AuthService` (BCrypt + SQL Server) để xác thực, ghi `login_logs`, rồi trả kết quả về Client.

---
Nếu bạn muốn, tôi có thể:
- Thêm ví dụ SQL tạo bảng
- Thêm hướng dẫn chạy Server/Client (build & run)
- Tạo `config.properties` mẫu

-- End of README
