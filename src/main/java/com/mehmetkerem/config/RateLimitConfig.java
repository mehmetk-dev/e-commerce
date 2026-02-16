package com.mehmetkerem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limiting ayarları.
 * application.properties'te rate-limit.auth-max-requests=5 gibi override edilebilir.
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    /** Auth (login, register, forgot-password) için dakikada max istek (IP başına). */
    private int authMaxRequests = 5;
    /** Auth penceresi (dakika). */
    private long authWindowMinutes = 1;

    /** Ödeme process için dakikada max istek (IP veya kullanıcı başına). */
    private int paymentMaxRequests = 10;
    private long paymentWindowMinutes = 1;
}
