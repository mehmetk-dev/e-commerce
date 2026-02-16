package com.mehmetkerem.service;

public interface INotificationService {
    void sendOrderConfirmation(String toEmail, String orderCode);

    void sendStockAlert(String productName);

    void sendPasswordResetLink(String toEmail, String resetUrl);

    void sendWelcomeEmail(String toEmail, String name);

    void sendOrderTrackingEmail(String toEmail, String orderCode, String trackingNumber, String carrier);
}
