package com.mehmetkerem.util;

import com.mehmetkerem.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Oturum açmış kullanıcıyı SecurityContext'ten almak için yardımcı sınıf.
 * IDOR önleme: controller'larda path'teki userId yerine bu sınıf kullanılmalı.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * SecurityContext'teki principal'ı User olarak döner.
     * JWT ile giriş yapılmışsa principal {@link User} tipindedir.
     *
     * @return mevcut kullanıcı veya null (oturum yoksa)
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof User ? (User) principal : null;
    }

    /**
     * Oturum açmış kullanıcının ID'sini döner.
     *
     * @return current user id veya null
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
