package com.mehmetkerem.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    @DisplayName("Role - tüm değerler mevcut")
    void role_AllValuesExist() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.VENDOR, Role.valueOf("VENDOR"));
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(3, Role.values().length);
    }

    @Test
    @DisplayName("OrderStatus - tüm değerler mevcut")
    void orderStatus_AllValuesExist() {
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.valueOf("DELIVERED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
        assertEquals(OrderStatus.PAID, OrderStatus.valueOf("PAID"));
        assertEquals(5, OrderStatus.values().length);
    }

    @Test
    @DisplayName("PaymentStatus - tüm değerler mevcut")
    void paymentStatus_AllValuesExist() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.PAID, PaymentStatus.valueOf("PAID"));
        assertEquals(PaymentStatus.UNPAID, PaymentStatus.valueOf("UNPAID"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
        assertEquals(4, PaymentStatus.values().length);
    }

    @Test
    @DisplayName("PaymentMethod - tüm değerler mevcut")
    void paymentMethod_AllValuesExist() {
        assertEquals(PaymentMethod.CREDIT_CARD, PaymentMethod.valueOf("CREDIT_CARD"));
        assertEquals(PaymentMethod.EFT, PaymentMethod.valueOf("EFT"));
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.valueOf("CASH_ON_DELIVERY"));
        assertEquals(3, PaymentMethod.values().length);
    }

    @Test
    @DisplayName("AuthProvider - tüm değerler mevcut")
    void authProvider_AllValuesExist() {
        assertEquals(AuthProvider.LOCAL, AuthProvider.valueOf("LOCAL"));
        assertEquals(AuthProvider.GOOGLE, AuthProvider.valueOf("GOOGLE"));
        assertEquals(AuthProvider.FACEBOOK, AuthProvider.valueOf("FACEBOOK"));
        assertEquals(3, AuthProvider.values().length);
    }
}
