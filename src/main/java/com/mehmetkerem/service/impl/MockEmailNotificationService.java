package com.mehmetkerem.service.impl;

import com.mehmetkerem.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("test")
public class MockEmailNotificationService implements INotificationService {

    @Override
    public void sendOrderConfirmation(String toEmail, String orderCode) {
        log.info("📧 [MOCK EMAIL] To: {} | Subject: Sipariş Onayı | Body: Siparişiniz alındı! Kod: {}", toEmail,
                orderCode);
    }

    @Override
    public void sendStockAlert(String productName, int currentStock) {
        log.warn("⚠️ [MOCK ALERT] Stok Tükeniyor! Ürün: {} — Kalan: {}", productName, currentStock);
    }

    @Override
    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        log.info("🔑 [MOCK EMAIL] To: {} | Subject: Şifre Sıfırlama | Body: Şifrenizi sıfırlamak için tıklayın: {}",
                toEmail,
                resetUrl);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        log.info("👋 [MOCK EMAIL] To: {} | Subject: Hoş Geldiniz | Body: Hoş geldin {}", toEmail, name);
    }

    @Override
    public void sendOrderTrackingEmail(String toEmail, String orderCode, String trackingNumber, String carrier) {
        log.info(
                "🚚 [MOCK EMAIL] To: {} | Subject: Kargo Takip | Body: Sipariş {} yola çıktı. Takip No: {} (Kargo: {})",
                toEmail, orderCode, trackingNumber, carrier);
    }

    @Override
    public void sendOrderStatusUpdate(String toEmail, String orderCode, String statusLabel) {
        log.info("📬 [MOCK EMAIL] To: {} | Subject: Durum Güncelleme | Body: Sipariş {} durumu: {}",
                toEmail, orderCode, statusLabel);
    }
}
