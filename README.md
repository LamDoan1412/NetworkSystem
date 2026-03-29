# 🚀 Network Login System

Hệ thống đăng nhập **Client - Server** sử dụng **Java Swing + Socket TCP + SQL Server** (Maven Multi-module).

📚 Đây là đồ án môn **Lập trình Mạng**.

---

## 📌 Giới thiệu

Project xây dựng một hệ thống đăng nhập hoàn chỉnh theo mô hình **Client - Server**, trong đó:

- **Client**: Giao diện người dùng (Java Swing)  
- **Server**: Xử lý logic, xác thực và kết nối database  
- **Database**: Lưu trữ tài khoản và lịch sử đăng nhập  

Các thành phần giao tiếp với nhau thông qua **Socket TCP**, đảm bảo truyền dữ liệu ổn định.

---

## ✨ Tính năng chính

- 🔐 Đăng ký & Đăng nhập tài khoản  
- 👤 Phân quyền **Admin** và **User**  
- 🛠 Admin có thể:
  - Quản lý người dùng  
  - Xem danh sách tài khoản  
- 📜 Xem lịch sử đăng nhập  
- 🔒 Mã hóa mật khẩu bằng **BCrypt**  
- 🔄 Giao tiếp 2 chiều Client - Server (TCP Socket)  
- 💻 Giao diện trực quan bằng Java Swing  

---

## 🛠 Công nghệ sử dụng

- ☕ Java (JDK 11 / 17)  
- 📦 Maven (Multi-module Project)  
- 🖥 Java Swing (UI)  
- 🗄 Microsoft SQL Server  
- 🔐 BCrypt (Hash password)  
- 🌐 Socket Programming (TCP)  

---

## 📁 Cấu trúc dự án

```
NetworkSystem/
├── client/             
├── server/             
├── common/             
├── database_setup.sql  
├── pom.xml             
└── README.md
```

---

## ⚙️ Kiến trúc hệ thống

Client (Swing UI)
        │
        │  Socket TCP
        ▼
Server (Business Logic)
        │
        ▼
SQL Server (Database)

---

## 🚀 Hướng dẫn cài đặt & chạy

### 🔹 1. Yêu cầu môi trường

- JDK 11 hoặc 17  
- IntelliJ IDEA  
- Microsoft SQL Server + SSMS  
- Maven  

---

### 🔹 2. Clone project

```
git clone https://github.com/LamDoan1412/NetworkSystem.git
cd NetworkSystem
```

---

### 🔹 3. Mở project

- File → Open → chọn `pom.xml`
- Open as Project
- Chờ Maven load dependencies

---

### 🔹 4. Thiết lập Database

- Tạo database (ví dụ: NetworkSystemDB)
- Chạy file `database_setup.sql`

Cấu hình lại kết nối trong Server:

```java
String url = "jdbc:sqlserver://localhost:1433;databaseName=NetworkSystemDB";
String user = "sa";
String password = "your_password";
```

---


## ⚙️ Kiến trúc hệ thống

Client (Swing UI) ---> Socket TCP ---> Server (Business Logic) ---> SQL Server (Database)

---


### 🔹 5. Chạy chương trình

- Run Server trước  
- Sau đó run Client  

---

## 🔐 Bảo mật

- Sử dụng BCrypt để hash mật khẩu  
- Không lưu mật khẩu dạng plain text  

---

## 📚 Kiến thức học được

- Socket TCP trong Java  
- Client - Server Architecture  
- Maven Multi-module  
- SQL Server Integration  
- Java Swing UI  

---

## 👨‍💻 Tác giả

**Lâm họ Đoàn**  

