package com.mehmetkerem.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionMessagesTest {

    @Test
    @DisplayName("Tüm sabitler mevcut ve format kabul eder")
    void allConstants_ExistAndAcceptFormat() {
        assertNotNull(ExceptionMessages.CATEGORY_ALL_READY_EXISTS);
        assertNotNull(ExceptionMessages.ADDRESS_NOT_FOUND);
        assertNotNull(ExceptionMessages.EMAIL_ALL_READY_EXISTS);
        assertNotNull(ExceptionMessages.NOT_FOUND);
        assertNotNull(ExceptionMessages.PRODUCT_NOT_FOUND);
        assertNotNull(ExceptionMessages.SOME_PRODUCTS_NOT_FOUND);
        assertNotNull(ExceptionMessages.INSUFFICIENT_STOCK);
        assertNotNull(ExceptionMessages.UNKNOW_STOCK);
        assertNotNull(ExceptionMessages.PRODUCT_NOT_FOUND_IN_CART);
        assertNotNull(ExceptionMessages.CART_NOT_FOUND);
    }

    @Test
    @DisplayName("NOT_FOUND format - id ve entity adı")
    void notFound_Format() {
        String msg = String.format(ExceptionMessages.NOT_FOUND, 1L, "ürün");
        assertTrue(msg.contains("1"));
        assertTrue(msg.contains("ürün"));
    }

    @Test
    @DisplayName("INSUFFICIENT_STOCK format - ürün adı")
    void insufficientStock_Format() {
        String msg = String.format(ExceptionMessages.INSUFFICIENT_STOCK, "Antika Saat");
        assertTrue(msg.contains("Antika Saat"));
    }

    @Test
    @DisplayName("CATEGORY_ALL_READY_EXISTS format")
    void categoryAlreadyExists_Format() {
        String msg = String.format(ExceptionMessages.CATEGORY_ALL_READY_EXISTS, "Antika");
        assertTrue(msg.contains("Antika"));
    }

    @Test
    @DisplayName("EMAIL_ALL_READY_EXISTS format")
    void emailAlreadyExists_Format() {
        String msg = String.format(ExceptionMessages.EMAIL_ALL_READY_EXISTS, "test@test.com");
        assertTrue(msg.contains("test@test.com"));
    }
}
