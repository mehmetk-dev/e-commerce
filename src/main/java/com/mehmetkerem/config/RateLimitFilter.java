package com.mehmetkerem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Merkezi rate limiting filtresi.
 * <p>
 * İstek geldiğinde sırasıyla bucket'ları kontrol eder.
 * İlk eşleşen bucket'ın limiti uygulanır.
 * Hiçbir bucket eşleşmezse global limit uygulanır.
 * <p>
 * Tüm ayarlar {@link RateLimitConfig} üzerinden application.properties'ten
 * yönetilir.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig config;

    /** Key: "bucketName:clientIp" veya "global:clientIp" → timestamp listesi */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> storage = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        // 1) Bucket eşleştirme — ilk eşleşen uygulanır
        for (RateLimitConfig.BucketConfig bucket : config.getBuckets()) {
            if (path.startsWith(bucket.getPathPrefix())) {
                if (!allowRequest(bucket.getName(), clientIp, bucket.getMaxRequests(), bucket.getWindowMinutes())) {
                    log.warn("Rate limit aşıldı: bucket={}, ip={}", bucket.getName(), clientIp);
                    rejectRequest(response, bucket.getMessage());
                    return;
                }
                // Bucket bulundu ve limit aşılmadı → devam et
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2) Hiçbir bucket eşleşmedi → global limit
        if (!allowRequest("global", clientIp, config.getGlobalMaxRequests(), config.getGlobalWindowMinutes())) {
            log.warn("Global rate limit aşıldı: ip={}", clientIp);
            rejectRequest(response, "İstek limiti aşıldı. Lütfen daha sonra tekrar deneyin.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ─── Ortak metodlar ───────────────────────────────────────────────

    private boolean allowRequest(String bucketName, String clientIp, int maxRequests, long windowMinutes) {
        String key = bucketName + ":" + clientIp;
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

    private void rejectRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    /**
     * Her 5 dakikada bir eski/boş entry'leri temizler — memory leak önlemi.
     */
    @Scheduled(fixedRate = 300_000)
    public void cleanupStorage() {
        long now = System.currentTimeMillis();
        // En büyük window süresini bul
        long maxWindowMinutes = config.getGlobalWindowMinutes();
        for (RateLimitConfig.BucketConfig b : config.getBuckets()) {
            maxWindowMinutes = Math.max(maxWindowMinutes, b.getWindowMinutes());
        }
        long cutoff = now - TimeUnit.MINUTES.toMillis(maxWindowMinutes);

        storage.forEach((key, timestamps) -> {
            synchronized (timestamps) {
                timestamps.removeIf(ts -> ts < cutoff);
            }
            if (timestamps.isEmpty()) {
                storage.remove(key);
            }
        });
    }
}
