package com.login.client.ui;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình xem lịch sử đăng nhập
 */
public class LoginHistoryForm extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbFilter;
    private JSpinner spLimit;

    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_DANGER  = new Color(192, 57, 43);
    private static final Color COLOR_WARNING = new Color(243, 156, 18);
    private static final Color COLOR_INFO    = new Color(142, 68, 173);

    public LoginHistoryForm() {
        initUI();
        loadHistory();
    }

    private void initUI() {
        setTitle("Lich su Dang nhap");
        setSize(900, 550);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(240, 242, 245));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTable(),  BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(COLOR_PRIMARY);
        p.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("LICH SU DANG NHAP");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        // Bộ lọc
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filter.setOpaque(false);

        JLabel lblLimit = new JLabel("Hien thi:");
        lblLimit.setForeground(Color.WHITE);
        lblLimit.setFont(new Font("Arial", Font.PLAIN, 13));

        spLimit = new JSpinner(new SpinnerNumberModel(50, 10, 500, 10));
        spLimit.setPreferredSize(new Dimension(70, 28));
        ((JSpinner.DefaultEditor) spLimit.getEditor()).getTextField().setEditable(false);

        JLabel lblFilter = new JLabel("  Loc:");
        lblFilter.setForeground(Color.WHITE);
        lblFilter.setFont(new Font("Arial", Font.PLAIN, 13));

        cbFilter = new JComboBox<>(new String[]{"Tat ca", "SUCCESS", "FAILED", "LOCKED", "BLOCKED"});
        cbFilter.setPreferredSize(new Dimension(100, 28));

        JButton btnRefresh = new JButton("Tai lai");
        btnRefresh.setBackground(COLOR_SUCCESS);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadHistory());

        filter.add(lblLimit);
        filter.add(spLimit);
        filter.add(lblFilter);
        filter.add(cbFilter);
        filter.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(filter, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] columns = {"STT", "Username", "Thoi gian", "Dia chi IP", "Trang thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);

        // Căn giữa cột STT
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Màu sắc theo status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 4);
                    switch (status) {
                        case "SUCCESS":
                            c.setBackground(new Color(220, 255, 220));
                            break;
                        case "FAILED":
                            c.setBackground(new Color(255, 235, 235));
                            break;
                        case "LOCKED":
                            c.setBackground(new Color(255, 245, 220));
                            break;
                        case "BLOCKED":
                            c.setBackground(new Color(255, 220, 220));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                
                if (column == 0) setHorizontalAlignment(SwingConstants.CENTER);
                else setHorizontalAlignment(SwingConstants.LEFT);
                
                return c;
            }
        });

        // Độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Time
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // IP
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Status

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        p.setBackground(new Color(240, 242, 245));

        JLabel lblInfo = new JLabel("Tong so ban ghi: 0");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 13));
        lblInfo.setForeground(new Color(52, 73, 94));

        p.add(lblInfo);
        return p;
    }

    // ==================== Load data ====================

    /**
     * Lấy dữ liệu trực tiếp từ DB (tạm thời cho đơn giản)
     * Lý tưởng nên gọi qua Server bằng Socket
     */
    private List<String[]> getLoginHistoryFromDB(int limit) {
        List<String[]> logs = new ArrayList<>();
        
        // Đọc cấu hình DB từ server (hoặc hard-code tạm)
        String url = "jdbc:sqlserver://HOLAD1412\\SQLEXPRESS;databaseName=LoginSystem;trustServerCertificate=true;encrypt=false;";
        String user = "loginadmin";
        String pass = "Admin@123";
        
        String sql = "SELECT TOP " + limit + " username, login_time, ip_address, status " +
                     "FROM login_logs ORDER BY login_time DESC";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(new String[]{
                    rs.getString("username"),
                    rs.getString("login_time"),
                    rs.getString("ip_address"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Loi ket noi database: " + e.getMessage(),
                "Loi", JOptionPane.ERROR_MESSAGE);
        }
        return logs;
    }

    private void loadHistory() {
        tableModel.setRowCount(0);

        int limit = (int) spLimit.getValue();
        String filterStatus = (String) cbFilter.getSelectedItem();

        List<String[]> logs = getLoginHistoryFromDB(limit);

        int stt = 1;
        for (String[] log : logs) {
            String status = log[3];
            
            // Lọc theo status nếu không phải "Tat ca"
            if (!"Tat ca".equals(filterStatus) && !filterStatus.equals(status)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                stt++,
                log[0],  // username
                log[1],  // login_time
                log[2],  // ip_address
                status   // status
            });
        }

        // Cập nhật footer
        JLabel lblInfo = (JLabel) ((JPanel) getContentPane().getComponent(2)).getComponent(0);
        lblInfo.setText("Tong so ban ghi: " + (stt - 1));
    }
}
