package com.mehmetkerem.util;

import com.mehmetkerem.dto.response.CursorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class ResultHelperTest {

    @Test
    @DisplayName("success - ResultData doğru döner")
    void success_ShouldReturnProperResultData() {
        String data = "Test Data";
        ResultData<String> result = ResultHelper.success(data);

        assertTrue(result.isStatus());
        assertEquals("İşlem Başarılı", result.getMessage());
        assertEquals("200", result.getCode());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("error - hata Result döner")
    void error_ShouldReturnProperResult() {
        String message = "Hata Mesajı";
        Result result = ResultHelper.error(message, HttpStatus.BAD_REQUEST);

        assertFalse(result.isStatus());
        assertEquals(message, result.getMessage());
        assertEquals("400", result.getCode());
    }

    @Test
    @DisplayName("validateError - validasyon hatası ResultData döner")
    void validateError_ShouldReturnProperResultData() {
        java.util.Map<String, String> errors = java.util.Map.of("field", "error");
        ResultData<java.util.Map<String, String>> result = ResultHelper.validateError(errors);

        assertFalse(result.isStatus());
        assertEquals("Validasyon Hatası", result.getMessage());
        assertEquals("400", result.getCode());
        assertEquals(errors, result.getData());
    }

    @Test
    @DisplayName("ok - başarı Result (data yok)")
    void ok_ShouldReturnSuccessResult() {
        Result result = ResultHelper.ok();
        assertTrue(result.isStatus());
        assertEquals("İşlem Başarılı", result.getMessage());
        assertEquals("200", result.getCode());
    }

    @Test
    @DisplayName("notFoundError - 404 Result döner")
    void notFoundError_ShouldReturn404Result() {
        Result result = ResultHelper.notFoundError("Kayıt bulunamadı");
        assertFalse(result.isStatus());
        assertEquals("Kayıt bulunamadı", result.getMessage());
        assertEquals("404", result.getCode());
    }

    @Test
    @DisplayName("cursor - sayfalı veri ResultData ile döner")
    void cursor_ShouldReturnPagedResultData() {
        List<String> items = List.of("a", "b");
        org.springframework.data.domain.Page<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 2);
        ResultData<CursorResponse<String>> result = ResultHelper.cursor(page);
        assertTrue(result.isStatus());
        CursorResponse<String> data = result.getData();
        assertNotNull(data);
        assertEquals(0, data.getPageNumber());
        assertEquals(10, data.getPageSize());
        assertEquals(2, data.getTotalElement());
        assertEquals(items, data.getItems());
    }
}
