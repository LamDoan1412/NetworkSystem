package com.login.client.ui;

import com.login.client.Client;
import com.login.common.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private JLabel lblConnectionStatus;

    private Client client;

    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_ERROR   = new Color(192, 57, 43);
    private static final Color COLOR_WARNING = new Color(243, 156, 18);

    public LoginForm() {
        this.client = new Client();
        initUI();
        connectToServer();
    }

    private void initUI() {
        setTitle("Network Login System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 420);
        setResizable(false);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) client.disconnect();
            }
        });

        setContentPane(buildMain());
    }

    private JPanel buildMain() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ── Header ──
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 2));
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(18, 20, 15, 20));

        JLabel icon = new JLabel("\uD83D\uDD10", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel title = new JLabel("NETWORK LOGIN SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        lblConnectionStatus = new JLabel("Đang kết nối...", SwingConstants.CENTER);
        lblConnectionStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        lblConnectionStatus.setForeground(new Color(255, 220, 100));

        header.add(icon);
        header.add(title);
        header.add(lblConnectionStatus);

        // ── Form ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 35, 15, 35));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;

        // Username label
        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        JLabel lUser = new JLabel("Tên đăng nhập:");
        lUser.setFont(new Font("Arial", Font.BOLD, 13));
        form.add(lUser, g);

        // Username field
        g.gridy = 1; g.insets = new Insets(0, 0, 12, 0);
        txtUsername = new JTextField();
        styleField(txtUsername);
        form.add(txtUsername, g);

        // Password label
        g.gridy = 2; g.insets = new Insets(0, 0, 4, 0);
        JLabel lPass = new JLabel("Mật khẩu:");
        lPass.setFont(new Font("Arial", Font.BOLD, 13));
        form.add(lPass, g);

        // Password field
        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('\u25CF');
        styleField(txtPassword);
        txtPassword.addActionListener(e -> performLogin());
        form.add(txtPassword, g);

        // Checkbox hien mat khau
        g.gridy = 4; g.insets = new Insets(0, 0, 14, 0);
        JCheckBox chk = new JCheckBox("Hiện mật khẩu");
        chk.setFont(new Font("Arial", Font.PLAIN, 12));
        chk.setBackground(Color.WHITE);
        chk.addActionListener(e -> txtPassword.setEchoChar(chk.isSelected() ? '\0' : '\u25CF'));
        form.add(chk, g);

        // Nut dang nhap
        g.gridy = 5; g.insets = new Insets(0, 0, 8, 0);
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBackground(COLOR_PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> performLogin());
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btnLogin.isEnabled()) btnLogin.setBackground(COLOR_PRIMARY.darker()); }
            public void mouseExited(MouseEvent e)  { btnLogin.setBackground(COLOR_PRIMARY); }
        });
        form.add(btnLogin, g);

        // Label trang thai
        g.gridy = 6; g.insets = new Insets(0, 0, 0, 0);
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setPreferredSize(new Dimension(0, 32));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(245, 245, 245));
        lblStatus.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        form.add(lblStatus, g);

        // ── Footer ──
        JPanel footer = new JPanel();
        footer.setBackground(new Color(248, 248, 248));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JLabel lf = new JLabel("TCP Socket + SQL Server  |  v1.0");
        lf.setFont(new Font("Arial", Font.PLAIN, 11));
        lf.setForeground(Color.GRAY);
        footer.add(lf);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void connectToServer() {
        btnLogin.setEnabled(false);
        new Thread(() -> {
            boolean ok = client.connect();
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    lblConnectionStatus.setText("\u2713 Đã kết nối: " + client.getHost() + ":" + client.getPort());
                    lblConnectionStatus.setForeground(new Color(150, 255, 150));
                    btnLogin.setEnabled(true);
                } else {
                    lblConnectionStatus.setText("\u2717 Không thể kết nối server!");
                    lblConnectionStatus.setForeground(new Color(255, 150, 150));
                }
            });
        }).start();
    }

    private void performLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty()) { showStatus("Vui long nhap ten dang nhap!", COLOR_WARNING); txtUsername.requestFocus(); return; }
        if (pass.isEmpty()) { showStatus("Vui long nhap mat khau!", COLOR_WARNING); txtPassword.requestFocus(); return; }

        setFormEnabled(false);
        showStatus("Dang xac thuc...", COLOR_PRIMARY);

        new Thread(() -> {
            Message res = client.login(user, pass);
            SwingUtilities.invokeLater(() -> handleResponse(res));
        }).start();
    }

    private void handleResponse(Message res) {
        setFormEnabled(true);
        switch (res.getType()) {
            case LOGIN_SUCCESS:
                showStatus("\u2713 " + res.getMessage(), COLOR_SUCCESS);
                Timer t = new Timer(1000, e -> {
                    new DashboardForm(res, client).setVisible(true);
                    dispose();
                });
                t.setRepeats(false); t.start();
                break;
            case LOGIN_FAILED:
                showStatus("\u2717 " + res.getMessage(), COLOR_ERROR);
                txtPassword.setText(""); txtPassword.requestFocus();
                shake();
                break;
            case LOGIN_LOCKED:
                showStatus("\uD83D\uDD12 " + res.getMessage(), COLOR_ERROR);
                setFormEnabled(false);
                break;
            default:
                showStatus("Phản hồi không xác định !", COLOR_WARNING);
        }
    }

    private void showStatus(String text, Color color) {
        lblStatus.setText(text);
        lblStatus.setForeground(color.darker());
        lblStatus.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
    }

    private void setFormEnabled(boolean en) {
        txtUsername.setEnabled(en);
        txtPassword.setEnabled(en);
        btnLogin.setEnabled(en && client.isConnected());
    }

    private void shake() {
        Point o = getLocation();
        int[] moves = {-8, 8, -8, 8, 0};
        Timer t = new Timer(40, null);
        int[] i = {0};
        t.addActionListener(e -> {
            setLocation(o.x + moves[i[0]], o.y);
            if (++i[0] >= moves.length) { setLocation(o); t.stop(); }
        });
        t.start();
    }
}