package com.mehmetkerem.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultDataTest {

    @Test
    @DisplayName("ResultData - data ile oluşturulur ve getter'lar çalışır")
    void resultData_ShouldHoldDataAndInheritResultFields() {
        String data = "test-data";
        ResultData<String> result = new ResultData<>(true, "OK", "200", data);

        assertTrue(result.isStatus());
        assertEquals("OK", result.getMessage());
        assertEquals("200", result.getCode());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("ResultData - generic tip korunur")
    void resultData_GenericType_ShouldBePreserved() {
        Integer value = 42;
        ResultData<Integer> result = new ResultData<>(true, "İşlem", "200", value);

        assertEquals(42, result.getData());
        assertTrue(result.getData() instanceof Integer);
    }

    @Test
    @DisplayName("ResultData - null data kabul eder")
    void resultData_NullData_ShouldBeAllowed() {
        ResultData<String> result = new ResultData<>(false, "Bulunamadı", "404", null);

        assertNull(result.getData());
        assertEquals("404", result.getCode());
    }
}
