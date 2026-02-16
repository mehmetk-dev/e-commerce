package com.mehmetkerem.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    @DisplayName("DELETE_VALUE - format doğru uygulanır")
    void deleteValue_Format_ShouldProduceExpectedMessage() {
        String result = String.format(Messages.DELETE_VALUE, 1, "ürün");
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.toLowerCase().contains("ürün") || result.contains("silinmiştir"));
    }

    @Test
    @DisplayName("CLEAR_VALUE - format doğru uygulanır")
    void clearValue_Format_ShouldProduceExpectedMessage() {
        String result = String.format(Messages.CLEAR_VALUE, 1, "sepet");
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.toLowerCase().contains("sepet") || result.contains("temizlenmiştir"));
    }

    @Test
    @DisplayName("PRODUCTS_NOT_FOUND - format doğru uygulanır")
    void productsNotFound_Format_ShouldContainTitle() {
        String result = String.format(Messages.PRODUCTS_NOT_FOUND, "Antika Saat");
        assertNotNull(result);
        assertTrue(result.contains("Antika Saat"));
    }

    @Test
    @DisplayName("Sabitler null değildir")
    void constants_ShouldNotBeNull() {
        assertNotNull(Messages.DELETE_VALUE);
        assertNotNull(Messages.CLEAR_VALUE);
        assertNotNull(Messages.PRODUCTS_NOT_FOUND);
    }
}
