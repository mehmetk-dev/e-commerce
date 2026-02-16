package com.mehmetkerem.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    @DisplayName("BaseException - mesaj ve status tutulur")
    void baseException_ShouldHoldMessageAndStatus() {
        BaseException ex = new BaseException("Test hata", HttpStatus.BAD_REQUEST);

        assertEquals("Test hata", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("NotFoundException - 404 status döner")
    void notFoundException_ShouldHave404Status() {
        NotFoundException ex = new NotFoundException("Kayıt bulunamadı");

        assertEquals("Kayıt bulunamadı", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("BadRequestException - 400 status döner")
    void badRequestException_ShouldHave400Status() {
        BadRequestException ex = new BadRequestException("Geçersiz istek");

        assertEquals("Geçersiz istek", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Exception - throw edilebilir")
    void exceptions_ShouldBeThrowable() {
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("test");
        });
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException("test");
        });
    }
}
