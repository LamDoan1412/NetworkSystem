package com.login.server;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Giao diện Server với popup xác nhận request từ Client
 */
public class ServerGUI extends JFrame {

    private JTextArea logArea;
    private JButton btnStart;
    private JButton btnStop;
    private Server server;

    public ServerGUI() {
        setTitle("Server - Network Login System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        // ===== HEADER =====
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(46, 117, 182));
        headerPanel.setPreferredSize(new Dimension(700, 70));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SERVER CONTROL PANEL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitleLabel = new JLabel("Network Login System - Approval Mode", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // ===== LOG AREA =====
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Server Logs"));
        add(scrollPane, BorderLayout.CENTER);

        // ===== CONTROL PANEL =====
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(240, 240, 240));

        btnStart = new JButton("▶ Start Server");
        btnStart.setFont(new Font("Arial", Font.BOLD, 14));
        btnStart.setBackground(new Color(76, 175, 80));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setPreferredSize(new Dimension(150, 40));

        btnStop = new JButton("⬛ Stop Server");
        btnStop.setFont(new Font("Arial", Font.BOLD, 14));
        btnStop.setBackground(new Color(244, 67, 54));
        btnStop.setForeground(Color.WHITE);
        btnStop.setFocusPainted(false);
        btnStop.setPreferredSize(new Dimension(150, 40));
        btnStop.setEnabled(false);

        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());

        controlPanel.add(btnStart);
        controlPanel.add(btnStop);
        add(controlPanel, BorderLayout.SOUTH);

        log("[GUI] Server GUI khoi dong thanh cong");
        log("[GUI] Click 'Start Server' de bat dau lang nghe...");
    }

    private void startServer() {
        server = new Server(this);
        new Thread(() -> server.start()).start();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        log("[SERVER] Da khoi dong, dang lang nghe tai port 9999...");
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        log("[SERVER] Da dung");
    }

    /**
     * Ghi log vào text area
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Hiện popup xác nhận request từ Client
     * @param username Username của người yêu cầu
     * @param action Hành động muốn thực hiện
     * @return CompletableFuture<Boolean> - true nếu approved, false nếu rejected
     */
    public CompletableFuture<Boolean> showApprovalDialog(String username, String action) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        SwingUtilities.invokeLater(() -> {
            // Tạo custom panel cho dialog
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Icon
            JLabel iconLabel = new JLabel("⚠", JLabel.CENTER);
            iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
            iconLabel.setForeground(new Color(255, 152, 0));
            panel.add(iconLabel, BorderLayout.WEST);

            // Message
            JPanel textPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            JLabel titleLabel = new JLabel("YEU CAU TU CLIENT");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

            JLabel userLabel = new JLabel("User: " + username);
            userLabel.setFont(new Font("Arial", Font.PLAIN, 12));

            JLabel actionLabel = new JLabel("Hanh dong: " + action);
            actionLabel.setFont(new Font("Arial", Font.PLAIN, 12));

            textPanel.add(titleLabel);
            textPanel.add(userLabel);
            textPanel.add(actionLabel);
            panel.add(textPanel, BorderLayout.CENTER);

            // Hiện dialog
            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Xac nhan request",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            boolean approved = (result == JOptionPane.YES_OPTION);
            future.complete(approved);

            // Log kết quả
            if (approved) {
                log("[APPROVED] " + username + " - " + action);
            } else {
                log("[REJECTED] " + username + " - " + action);
            }
        });

        return future;
    }

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}