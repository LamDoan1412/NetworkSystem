package com.login.client.ui;

import com.login.common.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý User - chỉ Admin mới truy cập được
 */
public class UserManagementForm extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_DANGER  = new Color(185, 12, 234);
    private static final Color COLOR_WARNING = new Color(230, 126, 34);

    // Cấu hình DB - kết nối trực tiếp port 1433
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=LoginSystem;trustServerCertificate=true;encrypt=false;";
    private static final String DB_USER = "loginadmin";
    private static final String DB_PASS = "Admin@123";

    public UserManagementForm() {
        initUI();
        loadUsers();
    }

    private void initUI() {
        setTitle("Quản lý người dùng");
        setSize(800, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(240, 242, 245));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTable(),  BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_PRIMARY);
        p.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("Tải lại");
        btnRefresh.setBackground(COLOR_SUCCESS);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadUsers());

        p.add(title, BorderLayout.WEST);
        p.add(btnRefresh, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] columns = {"Username", "Role", "Trạng thái", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        p.setBackground(new Color(240, 242, 245));

        addButton(p, "Thêm User", COLOR_SUCCESS, e -> addUser());
        addButton(p, "Khoá/ Mở khoá", COLOR_WARNING, e -> toggleStatus());
        addButton(p, "Đổi mật khẩu", COLOR_PRIMARY, e -> changePasswordUI());
        addButton(p, "Xoá User", COLOR_DANGER, e -> deleteUser());

        return p;
    }

    private void addButton(JPanel parent, String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        parent.add(btn);
    }

    // ==================== Database Methods ====================

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private List<String[]> getAllUsersFromDB() {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT username, role, is_active, created_at FROM users ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String status = rs.getBoolean("is_active") ? "Hoat dong" : "Bi khoa";
                users.add(new String[]{
                        rs.getString("username"),
                        rs.getString("role"),
                        status,
                        rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Loi ket noi DB: " + e.getMessage());
        }
        return users;
    }

    private boolean userExists(String username) {
        String sql = "SELECT TOP 1 id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean createUserInDB(String username, String passwordHash, String role) {
        String sql = "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, ?, 1)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Loi tao user: " + e.getMessage());
            return false;
        }
    }

    private boolean toggleUserStatusInDB(String username) {
        String sql = "UPDATE users SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    private boolean changePasswordInDB(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password = ?, updated_at = GETDATE() WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteUserFromDB(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xoá user: " + e.getMessage());
            return false;
        }
    }

    // ==================== UI Actions ====================

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<String[]> users = getAllUsersFromDB();
        for (String[] user : users) {
            tableModel.addRow(user);
        }
    }

    private void addUser() {
        JTextField txtUser = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"user", "admin"});

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(txtUser);
        panel.add(new JLabel("Password:"));
        panel.add(txtPass);
        panel.add(new JLabel("Role:"));
        panel.add(cbRole);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Thêm user mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
            String role = (String) cbRole.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin");
                return;
            }

            if (userExists(username)) {
                JOptionPane.showMessageDialog(this, "Username đã tồn tại!");
                return;
            }

            String hash = PasswordUtil.hash(password);
            if (createUserInDB(username, hash, role)) {
                JOptionPane.showMessageDialog(this, "Thêm user thành công!");
                loadUsers();
            }
        }
    }

    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn user");
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn muốn " + ("Hoạt động".equals(currentStatus) ? "KHOÁ" : "MỞ KHOÁ") + " user '" + username + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (toggleUserStatusInDB(username)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                loadUsers();
            }
        }
    }

    private void changePasswordUI() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn user");
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);

        JPasswordField txtNewPass = new JPasswordField(15);
        JPasswordField txtConfirm = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(txtNewPass);
        panel.add(new JLabel("Xác nhận:"));
        panel.add(txtConfirm);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Đổi mật khẩu cho: " + username, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newPass = new String(txtNewPass.getPassword());
            String confirm = new String(txtConfirm.getPassword());

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!");
                return;
            }

            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }

            String hash = PasswordUtil.hash(newPass);
            if (changePasswordInDB(username, hash)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khâu thành công!");
            }
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn user!");
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);

        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "Không thể xoá tài khoản admin!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XOÁ user '" + username + "'?\nHành động này không thể hoàn tác!",
                "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteUserFromDB(username)) {
                JOptionPane.showMessageDialog(this, "Xoá user thành công!");
                loadUsers();
            }
        }
    }
}