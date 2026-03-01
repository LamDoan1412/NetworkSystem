# Network Login System

Ung dung dang nhap mang dung **TCP Socket + SQL Server + BCrypt**.

---

## Cau truc du an

```
NetworkLoginSystem/
├── pom.xml
├── database_setup.sql          ← Chay trong SSMS de tao DB
├── common/
│   └── Message.java, PasswordUtil.java
├── server/
│   ├── resources/config.properties  ← Cau hinh server + DB
│   └── java/.../server/
│       ├── Main.java, Server.java, ClientHandler.java, AuthService.java
│       └── db/DBConnection.java, UserDAO.java
└── client/
    ├── resources/client.properties
    └── java/.../client/
        ├── Main.java, Client.java
        └── ui/LoginForm.java, DashboardForm.java
```

---

## Huong dan chay

### Buoc 1: Chay file SQL trong SSMS
Mo SSMS → Ket noi vao HOLAD1412\SQLEXPRESS ( Hoặc database của máy trên SQLServer) → Mo file database_setup.sql → Nhan F5

### Buoc 2: Bat TCP/IP cho SQL Server
Mo **SQL Server Configuration Manager**:
1. SQL Server Network Configuration → Protocols for SQLEXPRESS
2. Click phai TCP/IP → **Enable**
3. Vao tab **IP Addresses** → IPAll → TCP Port = **1433**
4. Khoi dong lai SQL Server (SQLEXPRESS) trong Services

### Buoc 3: Bat SQL Server Browser
Vao **Services.msc** → Tim "SQL Server Browser" → Start + set Startup type = Automatic

### Buoc 4: Build
    Mở tab Maven ở góc phải IntelliJ
    Mở NetworkLoginSystem → Lifecycle
    Double-click clean → chờ hoàn thành
    Double-click install → chờ thấy BUILD SUCCESS

### Buoc 5: Chay Server truoc
Chay `server/Main.java` trong IntelliJ.
Server se tu dong tao tai khoan test: admin/Admin@123 va user1/User@123

### Buoc 6: Chay Client
Chay `client/Main.java` trong IntelliJ.

---

## Tai khoan test (tu dong tao)

| Username | Password  | Role  |
|----------|-----------|-------|
| admin    | Admin@123 | admin |
| user1    | User@123  | user  |

---

## Luu y Windows Authentication

File config.properties dang dung `db.windows.auth=true`.
Neu gap loi ket noi, thu doi sang SQL Server Authentication:

1. Trong SSMS: Click phai server → Properties → Security → chon "SQL Server and Windows Authentication mode"
2. Tao login: Security → Logins → New Login → dat username/password
3. Doi trong config.properties:
```properties
db.windows.auth=false
db.username=ten_login_cua_ban
db.password=mat_khau
```
