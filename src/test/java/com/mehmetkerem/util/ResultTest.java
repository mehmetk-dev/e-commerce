package com.mehmetkerem.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    @DisplayName("Result - constructor ve getter'lar doğru değer döner")
    void result_ShouldHoldStatusMessageAndCode() {
        Result result = new Result(true, "Başarılı", "200");

        assertTrue(result.isStatus());
        assertEquals("Başarılı", result.getMessage());
        assertEquals("200", result.getCode());
    }

    @Test
    @DisplayName("Result - hata durumu")
    void result_ErrorCase_ShouldHoldFalseStatus() {
        Result result = new Result(false, "Hata", "500");

        assertFalse(result.isStatus());
        assertEquals("500", result.getCode());
    }
}
