package com.mehmetkerem.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MockEmailNotificationServiceTest {

    private final MockEmailNotificationService service = new MockEmailNotificationService();

    @Test
    @DisplayName("sendOrderConfirmation - exception fırlatmaz")
    void sendOrderConfirmation_ShouldNotThrow() {
        assertDoesNotThrow(() -> service.sendOrderConfirmation("user@test.com", "ORD-1"));
    }

    @Test
    @DisplayName("sendStockAlert - exception fırlatmaz")
    void sendStockAlert_ShouldNotThrow() {
        assertDoesNotThrow(() -> service.sendStockAlert("Ürün Adı"));
    }

    @Test
    @DisplayName("sendPasswordResetLink - exception fırlatmaz")
    void sendPasswordResetLink_ShouldNotThrow() {
        assertDoesNotThrow(() -> service.sendPasswordResetLink("user@test.com", "https://reset.link"));
    }

    @Test
    @DisplayName("sendWelcomeEmail - exception fırlatmaz")
    void sendWelcomeEmail_ShouldNotThrow() {
        assertDoesNotThrow(() -> service.sendWelcomeEmail("user@test.com", "Kullanıcı"));
    }

    @Test
    @DisplayName("sendOrderTrackingEmail - exception fırlatmaz")
    void sendOrderTrackingEmail_ShouldNotThrow() {
        assertDoesNotThrow(() -> service.sendOrderTrackingEmail("user@test.com", "ORD-1", "TRK123", "Kargo A"));
    }
}
