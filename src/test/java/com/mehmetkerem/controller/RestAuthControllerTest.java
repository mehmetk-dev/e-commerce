package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestAuthControllerImpl;
import com.mehmetkerem.dto.request.*;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.model.User;
import com.mehmetkerem.service.impl.AuthService;
import com.mehmetkerem.util.ResultData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private RestAuthControllerImpl controller;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@test.com");
        registerRequest.setName("Test");
        registerRequest.setPassword("password123");
        registerRequest.setRole(com.mehmetkerem.enums.Role.USER);
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
        loginResponse = LoginResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .user(new com.mehmetkerem.dto.response.UserResponse())
                .build();
    }

    @Test
    @DisplayName("register - ResultData ile token döner")
    void register_ShouldReturnSuccessWithToken() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(Map.of("token", "jwt123"));

        ResultData<Map<String, String>> result = controller.register(registerRequest);

        assertTrue(result.isStatus());
        assertEquals("jwt123", result.getData().get("token"));
        verify(authService).register(registerRequest);
    }

    @Test
    @DisplayName("login - ResultData ile LoginResponse döner")
    void login_ShouldReturnSuccessWithLoginResponse() {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResultData<LoginResponse> result = controller.login(loginRequest);

        assertTrue(result.isStatus());
        assertEquals("token", result.getData().getAccessToken());
        verify(authService).login(loginRequest);
    }

    @Test
    @DisplayName("refreshToken - yeni token döner")
    void refreshToken_ShouldReturnNewTokens() {
        TokenRefreshRequest req = new TokenRefreshRequest();
        req.setRefreshToken("old-refresh");
        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(loginResponse);

        ResultData<LoginResponse> result = controller.refreshToken(req);

        assertTrue(result.isStatus());
        verify(authService).refreshToken(req);
    }

    @Test
    @DisplayName("forgotPassword - mesaj döner")
    void forgotPassword_ShouldReturnSuccessMessage() {
        ResultData<String> result = controller.forgotPassword("user@test.com");

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("e-postanıza"));
        verify(authService).forgotPassword("user@test.com");
    }

    @Test
    @DisplayName("resetPassword - mesaj döner")
    void resetPassword_ShouldReturnSuccessMessage() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setToken("reset-token");
        req.setNewPassword("newPass123");
        ResultData<String> result = controller.resetPassword(req);

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("değiştirildi"));
        verify(authService).resetPassword(req);
    }

    @Test
    @DisplayName("changePassword - mesaj döner")
    void changePassword_ShouldReturnSuccessMessage() {
        User user = User.builder().id(1L).build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        ResultData<String> result = controller.changePassword(req, auth);

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("güncellendi"));
        verify(authService).changePassword(eq(1L), eq("old"), eq("new"));
    }
}
