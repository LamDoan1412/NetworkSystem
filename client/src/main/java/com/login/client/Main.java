package com.login.client;

import com.login.client.ui.LoginForm;

import javax.swing.*;

/**
 * Entry point của Client.
 * Khởi chạy giao diện đăng nhập Swing.
 */
public class Main {

    public static void main(String[] args) {
        // Set Look & Feel hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Chạy trên Event Dispatch Thread của Swing
        SwingUtilities.invokeLater(() -> {
            LoginForm form = new LoginForm();
            form.setVisible(true);
        });
    }
}
