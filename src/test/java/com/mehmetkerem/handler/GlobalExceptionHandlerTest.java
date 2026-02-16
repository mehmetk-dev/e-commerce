package com.mehmetkerem.handler;

import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.util.Result;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleBadCredentials - 401 ve hata mesajı döner")
    void handleBadCredentials_ShouldReturn401AndMessage() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<Result> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertTrue(response.getBody().getMessage().contains("e-posta") || response.getBody().getMessage().contains("şifre"));
        assertEquals("401", response.getBody().getCode());
    }

    @Test
    @DisplayName("handleBaseException - NotFoundException 404 döner")
    void handleBaseException_WhenNotFoundException_ShouldReturn404() {
        NotFoundException ex = new NotFoundException("Ürün bulunamadı");

        ResponseEntity<Result> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertEquals("Ürün bulunamadı", response.getBody().getMessage());
        assertEquals("404", response.getBody().getCode());
    }

    @Test
    @DisplayName("handleBaseException - BadRequestException 400 döner")
    void handleBaseException_WhenBadRequestException_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Geçersiz istek");

        ResponseEntity<Result> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertEquals("Geçersiz istek", response.getBody().getMessage());
        assertEquals("400", response.getBody().getCode());
    }

    @Test
    @DisplayName("handleValidationErrors - 400 ve field hataları döner")
    void handleValidationErrors_ShouldReturn400WithFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "Geçerli e-posta giriniz"));
        bindingResult.addError(new FieldError("target", "password", "En az 8 karakter olmalı"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ResultData<Map<String, String>>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertEquals("400", response.getBody().getCode());
        assertNotNull(response.getBody().getData());
        assertEquals("Geçerli e-posta giriniz", response.getBody().getData().get("email"));
        assertEquals("En az 8 karakter olmalı", response.getBody().getData().get("password"));
    }

    @Test
    @DisplayName("handleGeneralException - 500 döner")
    void handleGeneralException_ShouldReturn500() {
        Exception ex = new RuntimeException("Beklenmeyen hata");

        ResponseEntity<Result> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertEquals("500", response.getBody().getCode());
    }

    @Test
    @DisplayName("handleOptimisticLockingFailure - 409 Conflict döner")
    void handleOptimisticLockingFailure_ShouldReturn409() {
        org.springframework.orm.ObjectOptimisticLockingFailureException ex =
                new org.springframework.orm.ObjectOptimisticLockingFailureException(Object.class, 1L);

        ResponseEntity<Result> response = handler.handleOptimisticLockingFailure(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isStatus());
        assertEquals("409", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Veri") || response.getBody().getMessage().contains("güncellendi"));
    }
}
