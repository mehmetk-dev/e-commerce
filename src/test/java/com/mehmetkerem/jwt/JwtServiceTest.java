package com.mehmetkerem.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET = "9a61563156616461666164616661646166616461666164616661646166616461";
    private static final long EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
        userDetails = User.builder()
                .username("test@test.com")
                .password("encoded")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("generateToken - geçerli token üretir")
    void generateToken_ShouldProduceValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("extractUsername - token'dan kullanıcı adı çıkarılır")
    void extractUsername_ShouldReturnUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("test@test.com", username);
    }

    @Test
    @DisplayName("isValid - aynı kullanıcı için true döner")
    void isValid_WhenSameUser_ShouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isValid(token, userDetails);

        assertTrue(valid);
    }

    @Test
    @DisplayName("isValid - farklı kullanıcı için false döner")
    void isValid_WhenDifferentUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.builder()
                .username("other@test.com")
                .password("x")
                .authorities(List.of())
                .build();

        boolean valid = jwtService.isValid(token, otherUser);

        assertFalse(valid);
    }

    @Test
    @DisplayName("generateToken - farklı kullanıcılar farklı token alır")
    void generateToken_DifferentUsers_ShouldProduceDifferentTokens() {
        UserDetails user2 = User.builder()
                .username("user2@test.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(user2);

        assertNotEquals(token1, token2);
        assertEquals("test@test.com", jwtService.extractUsername(token1));
        assertEquals("user2@test.com", jwtService.extractUsername(token2));
    }
}
