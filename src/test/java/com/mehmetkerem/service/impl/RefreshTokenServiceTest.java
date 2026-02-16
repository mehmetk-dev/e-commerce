package com.mehmetkerem.service.impl;

import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.model.RefreshToken;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.RefreshTokenRepository;
import com.mehmetkerem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken validToken;
    private RefreshToken expiredToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L); // 7 gün
        user = User.builder().id(1L).email("test@test.com").build();
        validToken = RefreshToken.builder()
                .id(1L)
                .token("valid-token-123")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        expiredToken = RefreshToken.builder()
                .id(2L)
                .token("expired-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();
    }

    @Test
    @DisplayName("createRefreshToken - kullanıcı yoksa NotFoundException")
    void createRefreshToken_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> refreshTokenService.createRefreshToken(999L));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRefreshToken - yeni token oluşturulur")
    void createRefreshToken_WhenUserExists_ShouldCreateAndReturnToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("createRefreshToken - eski token varsa silinir")
    void createRefreshToken_WhenExistingToken_ShouldDeleteOldThenCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(validToken));
        doNothing().when(refreshTokenRepository).delete(validToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.createRefreshToken(1L);

        verify(refreshTokenRepository).delete(validToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyExpiration - süresi dolmuş token BadRequestException")
    void verifyExpiration_WhenExpired_ShouldThrowAndDeleteToken() {
        doNothing().when(refreshTokenRepository).delete(expiredToken);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> refreshTokenService.verifyExpiration(expiredToken));

        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("verifyExpiration - geçerli token aynen döner")
    void verifyExpiration_WhenValid_ShouldReturnToken() {
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        assertNotNull(result);
        assertEquals(validToken.getToken(), result.getToken());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("findByToken - token bulunur")
    void findByToken_WhenExists_ShouldReturnOptionalWithToken() {
        when(refreshTokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(validToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken("valid-token-123");

        assertTrue(result.isPresent());
        assertEquals("valid-token-123", result.get().getToken());
    }

    @Test
    @DisplayName("deleteByUserId - kullanıcı tokenları silinir")
    void deleteByUserId_WhenUserExists_ShouldDeleteTokens() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.deleteByUser(user)).thenReturn(1);

        int result = refreshTokenService.deleteByUserId(1L);

        assertEquals(1, result);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("deleteByUserId - kullanıcı yoksa NotFoundException")
    void deleteByUserId_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> refreshTokenService.deleteByUserId(999L));
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }
}
