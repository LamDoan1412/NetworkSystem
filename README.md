1.1 Cài đặt JDK

Tải JDK 11 hoặc 17: https://adoptium.net/
Cài đặt và kiểm tra:

cmd  java -version
```
  Phải thấy: `openjdk version "11.0.x"` hoặc `"17.0.x"`

### **1.2 Cài đặt IntelliJ IDEA**
- Tải bản Community (miễn phí): https://www.jetbrains.com/idea/download/
- Cài đặt bình thường (Next → Next → Install)

### **1.3 Cài đặt SQL Server**
- Tải SQL Server 2019 Express: https://www.microsoft.com/sql-server/sql-server-downloads
- Chọn **"Basic"** installation
- Ghi nhớ server name, thường là: `DESKTOP-XXX\SQLEXPRESS` hoặc `COMPUTERNAME\SQLEXPRESS`

### **1.4 Cài đặt SQL Server Management Studio (SSMS)**
- Tải tại: https://aka.ms/ssmsfullsetup
- Cài đặt (dùng để quản lý database)

### **1.5 Kiểm tra Maven**
IntelliJ có Maven tích hợp sẵn, không cần cài thêm.

---

## 📁 BƯỚC 2: MỞ PROJECT TRONG INTELLIJ (5 phút)

### **2.1 Extract folder project**
Giải nén file `NetworkLoginSystem.zip` ra ví dụ: `D:\Projects\NetworkLoginSystem`

⚠️ **LƯU Ý:** Đường dẫn **KHÔNG ĐƯỢC** chứa tiếng Việt!
- ✅ Đúng: `D:\Projects\NetworkLoginSystem`
- ❌ Sai: `D:\Đồ án\Lập trình mạng\NetworkLoginSystem`

### **2.2 Mở project trong IntelliJ**
1. Mở IntelliJ IDEA
2. **File → Open**
3. Duyệt đến folder `NetworkLoginSystem`
4. **Chọn file `pom.xml`** (trong thư mục gốc)
5. Click **Open**
6. Trong dialog hiện ra, chọn **"Open as Project"**

### **2.3 Chờ IntelliJ load Maven**
- IntelliJ sẽ tự động:
  - Detect Maven project
  - Download dependencies (SQL Server JDBC, BCrypt...)
  - Build project structure
- Chờ thanh progress bar ở góc dưới chạy xong (1-3 phút)

### **2.4 Kiểm tra cấu trúc project**
Bên trái phải thấy:
```
📦 NetworkLoginSystem
├── 📦 common
├── 📦 server
│   └── src/main/java/com/login/server
└── 📦 client
    └── src/main/java/com/login/client
Nếu không thấy → Click nút Reload All Maven Projects (biểu tượng 🔄 trong tab Maven)

🗄️ BƯỚC 3: SETUP DATABASE (10 phút)
3.1 Mở SSMS và kết nối

Mở SQL Server Management Studio
Server name: Điền tên server của bạn (ví dụ: HOLAD1412\SQLEXPRESS)
Authentication: Windows Authentication
Click Connect

3.2 Chạy script tạo database

Trong IntelliJ, mở file database_setup.sql (ở thư mục gốc)
Copy toàn bộ nội dung (Ctrl+A → Ctrl+C)
Quay lại SSMS → File → New → Query
Paste script vào (Ctrl+V)
Click Execute (hoặc F5)
Thấy thông báo: "Database setup hoan tat!"

3.3 Tạo login cho SQL Server
Trong SSMS, mở New Query và chạy:
sql-- Bật mixed mode authentication
USE master;
GO

-- Tạo login
CREATE LOGIN loginadmin WITH PASSWORD = 'Admin@123',
CHECK_POLICY = OFF,
CHECK_EXPIRATION = OFF;
GO

-- Cấp quyền sysadmin
ALTER SERVER ROLE sysadmin ADD MEMBER loginadmin;
GO

-- Kiểm tra
SELECT name, is_disabled FROM sys.server_principals 
WHERE name = 'loginadmin';
GO
Phải thấy kết quả: loginadmin | 0 (is_disabled = 0)
3.4 Bật TCP/IP cho SQL Server

Mở SQL Server Configuration Manager (tìm trong Start menu)
Vào SQL Server Network Configuration → Protocols for SQLEXPRESS
Click phải TCP/IP → Enable
Click phải TCP/IP → Properties
Tab IP Addresses → IPAll
Điền: TCP Port = 1433
Click OK

3.5 Restart SQL Server

Vào Services.msc (Windows + R → gõ services.msc)
Tìm SQL Server (SQLEXPRESS)
Click phải → Restart
Tìm SQL Server Browser
Click phải → Start (nếu chưa chạy)
Click phải → Properties → Startup type → Automatic


⚙️ BƯỚC 4: CẤU HÌNH PROJECT (5 phút)
4.1 Cập nhật server name trong code
Nếu server name của bạn KHÔNG PHẢI HOLAD1412\SQLEXPRESS, cần sửa:
File 1: server/src/main/resources/config.properties
properties# Thay HOLAD1412\SQLEXPRESS bằng server name của bạn
db.server=TÊN_SERVER_CỦA_BẠN\\SQLEXPRESS
db.name=LoginSystem
db.windows.auth=false
db.username=loginadmin
db.password=Admin@123
File 2: client/src/main/java/com/login/client/ui/UserManagementForm.java
Tìm dòng 28:
java// Sửa localhost thành IP máy server nếu cần
private static final String DB_URL = "jdbc:sqlserver://localhost:1433;...";
File 3: client/src/main/java/com/login/client/ui/LoginHistoryForm.java
Tìm khoảng dòng 210:
javaString url = "jdbc:sqlserver://localhost:1433;...";
4.2 Kiểm tra kết nối Client
Mở client/src/main/resources/client.properties:
propertiesserver.host=localhost
server.port=9999
```

---

## 🏗️ BƯỚC 5: BUILD PROJECT (2 phút)

### **5.1 Mở tab Maven**
Góc phải IntelliJ → Click **Maven**

### **5.2 Clean**
**NetworkLoginSystem → Lifecycle → double-click `clean`**  
Chờ thấy `BUILD SUCCESS`

### **5.3 Install**
**Lifecycle → double-click `install`**  
Chờ thấy:
```
[INFO] NetworkLoginSystem ................. SUCCESS
[INFO] common ............................. SUCCESS
[INFO] server ............................. SUCCESS
[INFO] client ............................. SUCCESS
[INFO] BUILD SUCCESS
```

---

## ▶️ BƯỚC 6: CHẠY PROJECT (2 phút)

### **6.1 Chạy Server TRƯỚC**
1. Mở file `server/src/main/java/com/login/server/Main.java`
2. Click phải vào file → **Run 'Main.main()'**
3. Hoặc click nút **▶** xanh lá bên lề trái (dòng `public static void main`)

**Kết quả mong đợi:**
```
[DB] Ket noi SQL Server thanh cong!
[SEED] Tao tai khoan admin / Admin@123
[SEED] Tao tai khoan user1 / User@123
[SEED] Xong!
==================================================
   NETWORK LOGIN SYSTEM - SERVER
==================================================
[SERVER] Dang lang nghe tai port 9999 ...
```

⚠️ **ĐỂ NGUYÊN CỬA SỔ NÀY, KHÔNG TẮT!**

### **6.2 Chạy Client SAU**
1. Mở file `client/src/main/java/com/login/client/Main.java`
2. Click phải → **Run 'Main.main()'**
3. Hoặc click nút **▶** xanh lá

**Kết quả:** Cửa sổ đăng nhập hiện ra!

---

## 🎮 BƯỚC 7: TEST CHỨC NĂNG (5 phút)

### **7.1 Đăng nhập**
- Username: `admin`
- Password: `Admin@123`
- Click **ĐĂNG NHẬP**

Thấy: `✓ Đăng nhập thành công! Xin chào admin [admin]`

### **7.2 Vào Dashboard**
Sau 1 giây tự động mở Dashboard với 4 nút:
- Thông tin hệ thống
- Lịch sử đăng nhập
- Quản lý người dùng
- Đăng xuất

### **7.3 Test Quản lý người dùng**
Click **"Quan ly nguoi dung"** → thấy bảng với 2 user (admin, user1)

Thử:
- Click **"Them User"** → tạo user mới
- Chọn 1 user → Click **"Khoa/Mo khoa"**
- Chọn 1 user → Click **"Doi mat khau"**

### **7.4 Test Lịch sử đăng nhập**
Click **"Lich su dang nhap"** → thấy bảng log với màu xanh/đỏ

---

## 🚨 XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi 1: "Cannot resolve symbol UserDAO"**
**Nguyên nhân:** Maven chưa download xong dependencies  
**Fix:** Tab Maven → Click nút 🔄 **Reload All Maven Projects**

### **Lỗi 2: "No suitable driver found for jdbc:sqlserver"**
**Nguyên nhân:** Thiếu SQL Server JDBC driver  
**Fix:** Kiểm tra file `client/pom.xml` có dependency `mssql-jdbc` chưa

### **Lỗi 3: "Connection refused" khi chạy Client**
**Nguyên nhân:** Server chưa chạy  
**Fix:** Chạy Server TRƯỚC, chờ thấy "Đang lắng nghe..." rồi mới chạy Client

### **Lỗi 4: "Login failed for user 'loginadmin'"**
**Nguyên nhân:** Login chưa được tạo hoặc sai mật khẩu  
**Fix:** Chạy lại script tạo login ở **Bước 3.3**

### **Lỗi 5: "SocketTimeoutException"**
**Nguyên nhân:** SQL Server Browser chưa chạy hoặc port sai  
**Fix:** 
- Sửa connection string thành `localhost:1433`
- Hoặc bật SQL Server Browser trong Services

### **Lỗi 6: Path có ký tự lạ (??)**
**Nguyên nhân:** Đường dẫn có tiếng Việt  
**Fix:** Di chuyển project sang đường dẫn không dấu (ví dụ: `D:\Projects\`)

---

## ✅ CHECKLIST HOÀN THÀNH

- [ ] JDK đã cài (java -version OK)
- [ ] IntelliJ đã cài
- [ ] SQL Server đã cài
- [ ] SSMS đã cài
- [ ] Project đã extract vào thư mục không dấu
- [ ] Project đã mở trong IntelliJ (file pom.xml)
- [ ] Maven đã load xong dependencies
- [ ] Database `LoginSystem` đã tạo
- [ ] Login `loginadmin` đã tạo
- [ ] TCP/IP đã enable, port 1433
- [ ] SQL Server đã restart
- [ ] Maven build SUCCESS
- [ ] Server chạy thành công (thấy "Đang lắng nghe...")
- [ ] Client chạy thành công (cửa sổ login hiện ra)
- [ ] Đăng nhập admin thành công
- [ ] Tất cả chức năng đã test OK

---

## 📝 TÓM TẮT QUY TRÌNH NHANH
```
1. Cài JDK + IntelliJ + SQL Server + SSMS
2. Extract project vào D:\Projects\NetworkLoginSystem
3. IntelliJ: File → Open → chọn pom.xml
4. SSMS: Chạy database_setup.sql
5. SSMS: Chạy script tạo login loginadmin
6. SQL Server Configuration Manager: Enable TCP/IP port 1433
7. Services: Restart SQL Server (SQLEXPRESS)
8. IntelliJ Maven: clean → install
9. Chạy server/Main.java
10. Chạy client/Main.java
11. Đăng nhập admin/Admin@123
