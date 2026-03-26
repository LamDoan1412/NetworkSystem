package com.login.server;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class ServerGUI extends JFrame {

    private JTextArea logArea;
    private JButton btnStart;
    private JButton btnStop;
    private Server server;

    public ServerGUI() {
        setTitle("Server - Network Login System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildLogArea(), BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);

        log("[GUI] Server GUI khoi dong thanh cong");
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setPreferredSize(new Dimension(700, 70));

        JLabel title = new JLabel("SERVER CONTROL PANEL", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Network Login System - Server", JLabel.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(200, 200, 200));

        panel.add(title, BorderLayout.CENTER);
        panel.add(sub, BorderLayout.SOUTH);

        return panel;
    }

    // ================= LOG =================
    private JScrollPane buildLogArea() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Server Logs"));

        return scroll;
    }

    // ================= CONTROL =================
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(240, 240, 240));

        btnStart = createButton("Start Server",
                new Color(220, 245, 224),
                new Color(46, 125, 50));

        btnStop = createButton("Stop Server",
                new Color(255, 220, 220),
                new Color(198, 40, 40));

        btnStop.setEnabled(false);

        // 👉 QUAN TRỌNG (fix lỗi không chạy)
        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());

        panel.add(btnStart);
        panel.add(btnStop);

        return panel;
    }

    // ================= BUTTON STYLE =================
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // ================= SERVER =================
    private void startServer() {
        log("[DEBUG] Dang start server...");

        server = new Server(this);

        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                log("[ERROR] " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
        }

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        log("[SERVER] Da dung");
    }

    // ================= LOG =================
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = new java.text.SimpleDateFormat("HH:mm:ss")
                    .format(new java.util.Date());

            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // ================= DIALOG =================
    public CompletableFuture<Boolean> showApprovalDialog(String username, String action) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "User: " + username + "\nAction: " + action,
                    "Xac nhan request",
                    JOptionPane.YES_NO_OPTION
            );

            future.complete(result == JOptionPane.YES_OPTION);
        });

        return future;
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}