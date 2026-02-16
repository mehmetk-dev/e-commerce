package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.LoginRequest;
import com.mehmetkerem.dto.request.RegisterRequest;
import com.mehmetkerem.dto.response.LoginResponse;
import com.mehmetkerem.dto.response.UserResponse;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.jwt.JwtService;
import com.mehmetkerem.model.RefreshToken;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.PasswordResetTokenRepository;
import com.mehmetkerem.repository.UserRepository;
import com.mehmetkerem.service.INotificationService;
import com.mehmetkerem.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private IUserService userService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private INotificationService notificationService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("register - yeni kullanıcı kaydedilir ve hoş geldin maili gider")
    void register_ShouldSaveUserAndSendWelcomeEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");
        request.setPassword("password");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(encoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("testToken");

        Map<String, String> result = authService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
        verify(notificationService, times(1)).sendWelcomeEmail(eq("test@test.com"), eq("Test User"));
        assertEquals("testToken", result.get("token"));
    }

    @Test
    @DisplayName("register - email zaten kayıtlıysa BadRequestException")
    void register_WhenEmailExists_ShouldThrowBadRequestException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setName("User");
        request.setPassword("password");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - geçerli kimlik bilgileri ile token döner")
    void login_WhenValidCredentials_ShouldReturnLoginResponse() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test")
                .passwordHash("encoded")
                .role(Role.USER)
                .build();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@test.com");
        userResponse.setName("Test");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(RefreshToken.builder().token("refreshToken").build());
        when(userService.getUserResponseById(1L)).thenReturn(userResponse);

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
