package com.mehmetkerem.config;

import org.springframework.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Seçili path'lerde IP bazlı rate limiting (sliding window).
 * Auth ve payment endpoint'lerinde brute force / kötüye kullanımı sınırlar.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig config;

    /** Key: "auth:ip" veya "payment:ip", Value: son istek zamanları (ms). */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> storage = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientKey = clientKey(request);

        if (isAuthPath(path)) {
            if (!allowRequest("auth", clientKey, config.getAuthMaxRequests(), config.getAuthWindowMinutes())) {
                log.warn("Rate limit aşıldı: auth, key={}", clientKey);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Çok fazla deneme. Lütfen daha sonra tekrar deneyin.\"}");
                return;
            }
        } else if (isPaymentPath(path)) {
            if (!allowRequest("payment", clientKey, config.getPaymentMaxRequests(), config.getPaymentWindowMinutes())) {
                log.warn("Rate limit aşıldı: payment, key={}", clientKey);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Ödeme isteği limiti aşıldı. Lütfen kısa süre sonra tekrar deneyin.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private boolean isAuthPath(String path) {
        return path != null && path.startsWith("/v1/auth/");
    }

    private boolean isPaymentPath(String path) {
        return path != null && path.startsWith("/v1/payment/");
    }

    private boolean allowRequest(String bucket, String clientKey, int maxRequests, long windowMinutes) {
        String key = bucket + ":" + clientKey;
        long now = System.currentTimeMillis();
        long windowMs = TimeUnit.MINUTES.toMillis(windowMinutes);
        long cutoff = now - windowMs;

        CopyOnWriteArrayList<Long> timestamps = storage.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());

        synchronized (timestamps) {
            timestamps.removeIf(ts -> ts < cutoff);
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.add(now);
        }
        return true;
    }
}
