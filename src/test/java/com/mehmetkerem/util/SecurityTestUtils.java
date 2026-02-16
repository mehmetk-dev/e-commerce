package com.mehmetkerem.util;

import com.mehmetkerem.enums.AuthProvider;
import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/** Testlerde SecurityContext'e principal set etmek için yardımcı. */
public final class SecurityTestUtils {

    public static final Long DEFAULT_USER_ID = 1L;

    private SecurityTestUtils() {
    }

    /** SecurityContext'e id'si DEFAULT_USER_ID olan bir User principal set eder. */
    public static void setCurrentUser() {
        setCurrentUser(DEFAULT_USER_ID, Role.USER);
    }

    public static void setCurrentUser(Long userId, Role role) {
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@test.com")
                .passwordHash("hash")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    /** SecurityContext'i temizler (test sonrası). */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
