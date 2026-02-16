package com.mehmetkerem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthEntryPointTest {

    private AuthEntryPoint authEntryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException {
        authEntryPoint = new AuthEntryPoint();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        outputStream = new ByteArrayOutputStream();
        when(request.getServletPath()).thenReturn("/v1/order/save");
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener listener) {}
            @Override
            public void write(int b) throws IOException { outputStream.write(b); }
        });
    }

    @Test
    @DisplayName("commence - 401 status set edilir")
    void commence_ShouldSet401Status() throws Exception {
        AuthenticationException ex = new BadCredentialsException("Bad credentials");
        authEntryPoint.commence(request, response, ex);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
    }

    @Test
    @DisplayName("commence - JSON body'de error ve message vardır")
    void commence_ShouldWriteJsonBodyWithErrorAndMessage() throws Exception {
        AuthenticationException ex = new BadCredentialsException("Invalid token");
        authEntryPoint.commence(request, response, ex);

        byte[] written = outputStream.toByteArray();
        assertTrue(written.length > 0);
        String json = new String(written);
        assertTrue(json.contains("Unauthorized") || json.contains("error"));
        assertTrue(json.contains("message"));
        assertTrue(json.contains("path") || json.contains("/v1/"));
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(json, Map.class);
        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
    }
}
