package com.login.client.ui;

import com.login.client.Client;
import com.login.common.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardForm extends JFrame {

    private final Message loginInfo;
    private final Client client;
    private JLabel lblClock;

    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_DANGER  = new Color(192, 57, 43);
    private static final Color COLOR_ORANGE  = new Color(230, 126, 34);
    private static final Color COLOR_PURPLE  = new Color(142, 68, 173);
    private static final Color COLOR_BG      = new Color(240, 242, 245);

    public DashboardForm(Message loginInfo, Client client) {
        this.loginInfo = loginInfo;
        this.client    = client;
        initUI();
        startClock();
    }

    private void initUI() {
        setTitle("Dashboard - " + loginInfo.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(580, 480);
        setResizable(false);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (client != null) client.disconnect();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Header ──────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(COLOR_PRIMARY);
        h.setBorder(new EmptyBorder(16, 22, 16, 22));

        // Trai
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel lblHello = new JLabel( " Người dùng: " + loginInfo.getUsername() );
        lblHello.setFont(new Font("Arial", Font.BOLD, 20));
        lblHello.setForeground(Color.WHITE);

        boolean isAdmin = "admin".equalsIgnoreCase(loginInfo.getRole());
        JLabel lblBadge = new JLabel( "VAI TRÒ:" + ("  " + (isAdmin ? "ADMIN" : "USER") + "  "));
        lblBadge.setFont(new Font("Arial", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(isAdmin ? COLOR_PURPLE : COLOR_SUCCESS);
        lblBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        left.add(lblHello);
        left.add(lblBadge);

        // Phai: dong ho
        lblClock = new JLabel("", SwingConstants.RIGHT);
        lblClock.setFont(new Font("Arial", Font.PLAIN, 12));
        lblClock.setForeground(new Color(200, 230, 255));

        h.add(left,     BorderLayout.WEST);
        h.add(lblClock, BorderLayout.EAST);
        return h;
    }

    // ── Noi dung chinh ──────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));
        p.add(buildInfoCards(),   BorderLayout.NORTH);
        p.add(buildActionGrid(),  BorderLayout.CENTER);
        return p;
    }

    private JPanel buildInfoCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setBackground(COLOR_BG);

        boolean isAdmin = "admin".equalsIgnoreCase(loginInfo.getRole());
        addCard(row, "TÀI KHOẢN",  loginInfo.getUsername(), COLOR_PRIMARY);
        addCard(row, "QUYỀN HẠN",  loginInfo.getRole().toUpperCase(),
                isAdmin ? COLOR_PURPLE : COLOR_SUCCESS);
        addCard(row, "SERVER",     "localhost:9999", new Color(22, 160, 133));
        return row;
    }

    private void addCard(JPanel parent, String label, String value, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 215, 215)),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 15));
        val.setForeground(valueColor);

        card.add(lbl);
        card.add(val);
        parent.add(card);
    }

    private JPanel buildActionGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(COLOR_BG);

        boolean isAdmin = "admin".equalsIgnoreCase(loginInfo.getRole());

        addBtn(grid, "Thông tin hệ thống", COLOR_PRIMARY,
               e -> showSystemInfo());

        addBtn(grid, "Lịch sử đăng nhập", new Color(22, 160, 133),
               e -> new LoginHistoryForm().setVisible(true));

        JButton btnAdmin = addBtn(grid, "Quản lý người dùng", COLOR_PURPLE,
               e -> new UserManagementForm().setVisible(true));
        if (!isAdmin) {
            btnAdmin.setEnabled(false);
            btnAdmin.setToolTipText("Chỉ Admin mới có quyền này");
            btnAdmin.setBackground(new Color(180, 180, 180));
        }

        addBtn(grid, "Đăng xuất", COLOR_DANGER, e -> logout());

        return grid;
    }

    private JButton addBtn(JPanel parent, String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { if(btn.isEnabled()) btn.setBackground(bg); }
        });
        parent.add(btn);
        return btn;
    }

    // ── Footer ──────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setBackground(new Color(44, 62, 80));
        f.setBorder(new EmptyBorder(7, 18, 7, 18));

        JLabel left = new JLabel("Network Login System v1.0  |  TCP Socket + SQL Server");
        left.setFont(new Font("Arial", Font.PLAIN, 11));
        left.setForeground(new Color(150, 180, 200));

        JLabel right = new JLabel("Đã kết nối  [on]");
        right.setFont(new Font("Arial", Font.BOLD, 11));
        right.setForeground(COLOR_SUCCESS);

        f.add(left,  BorderLayout.WEST);
        f.add(right, BorderLayout.EAST);
        return f;
    }

    // ── Chuc nang ───────────────────────────────────────
    private void showSystemInfo() {
        String info =
            "Java version : " + System.getProperty("java.version") + "\n" +
            "OS           : " + System.getProperty("os.name")      + "\n" +
            "Server       : localhost:9999\n"                               +
            "Database     : SQL Server (HOLAD1412\\SQLEXPRESS)\n"           +
            "Người dùng   : " + loginInfo.getUsername()            + "\n" +
            "Quyền        : " + loginInfo.getRole()                + "\n" +
            "Thời gian    : " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        JOptionPane.showMessageDialog(this, info,
            "Thông tin hệ thống", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Bạn có muốn đăng xuất?", "Xác nhận ",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            client.disconnect();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }

    private void startClock() {
        Timer t = new Timer(1000, e ->
            lblClock.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy   HH:mm:ss")))
        );
        t.start();
        t.getActionListeners()[0].actionPerformed(null);
    }
}
