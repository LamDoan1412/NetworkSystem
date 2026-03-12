IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'LoginSystem')
BEGIN
    CREATE DATABASE LoginSystem;
    PRINT 'Da tao database LoginSystem';
END
GO

USE LoginSystem;
GO

-- ==================== Bang users ====================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id         INT           PRIMARY KEY IDENTITY(1,1),
        username   NVARCHAR(50)  NOT NULL UNIQUE,
        password   NVARCHAR(255) NOT NULL,          -- BCrypt hash
        role       NVARCHAR(20)  NOT NULL DEFAULT 'user',
        is_active  BIT           NOT NULL DEFAULT 1, -- 1=hoat dong, 0=bi khoa
        created_at DATETIME      DEFAULT GETDATE(),
        updated_at DATETIME      DEFAULT GETDATE()
    );
    PRINT 'Da tao bang users';
END
GO

-- ==================== Bang login_logs ====================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='login_logs' AND xtype='U')
BEGIN
    CREATE TABLE login_logs (
        id          INT           PRIMARY KEY IDENTITY(1,1),
        username    NVARCHAR(50)  NOT NULL,
        login_time  DATETIME      DEFAULT GETDATE(),
        ip_address  NVARCHAR(50),
        status      NVARCHAR(20)  NOT NULL   -- SUCCESS / FAILED / LOCKED / BLOCKED
    );

    -- Index de query nhanh hon
    CREATE INDEX IX_login_logs_username   ON login_logs(username);
    CREATE INDEX IX_login_logs_login_time ON login_logs(login_time);
    CREATE INDEX IX_login_logs_status     ON login_logs(status);

    PRINT 'Da tao bang login_logs';
END
GO

-- ==================== Ket qua ====================
PRINT '================================';
PRINT 'Setup hoan tat!';
PRINT 'Cac bang da tao:';
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE';
GO

-- ==================== Query xem du lieu ====================
-- Xem tat ca users:
-- SELECT * FROM users;

-- Xem lich su dang nhap gan day:
-- SELECT TOP 20 * FROM login_logs ORDER BY login_time DESC;

-- Thong ke dang nhap that bai trong 1 gio:
-- SELECT username, COUNT(*) AS so_lan_that_bai
-- FROM login_logs
-- WHERE status = 'FAILED'
--   AND login_time > DATEADD(HOUR, -1, GETDATE())
-- GROUP BY username
-- ORDER BY so_lan_that_bai DESC;
