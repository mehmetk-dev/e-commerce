package com.mehmetkerem.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Merkezi rate limit konfigürasyonu.
 * Tüm limitler application.properties'ten yönetilir.
 *
 * Örnek:
 * rate-limit.global-max-requests=60
 * rate-limit.buckets[0].name=auth
 * rate-limit.buckets[0].path-prefix=/v1/auth/
 * rate-limit.buckets[0].max-requests=5
 * rate-limit.buckets[0].window-minutes=1
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    /** Herhangi bir bucket'a uymayan tüm istekler için global limit (IP başına). */
    private int globalMaxRequests = 60;
    private long globalWindowMinutes = 1;

    /** Endpoint bazlı özel limitler. Sıra önemli — ilk eşleşen bucket uygulanır. */
    private List<BucketConfig> buckets = new ArrayList<>();

    @Data
    public static class BucketConfig {
        /** Bucket adı (loglarda görünür). */
        private String name;
        /** Path prefix — bu prefix ile başlayan istekler bu bucket'a düşer. */
        private String pathPrefix;
        /** Bu bucket için dakikada max istek sayısı (IP başına). */
        private int maxRequests;
        /** Sliding window süresi (dakika). */
        private long windowMinutes = 1;
        /** Limit aşıldığında döndürülecek mesaj. */
        private String message = "İstek limiti aşıldı. Lütfen daha sonra tekrar deneyin.";
    }

    /**
     * Hiç bucket tanımlanmamışsa varsayılan auth ve payment bucket'larını ekler.
     */
    @PostConstruct
    public void initDefaults() {
        if (buckets.isEmpty()) {
            BucketConfig auth = new BucketConfig();
            auth.setName("auth");
            auth.setPathPrefix("/v1/auth/");
            auth.setMaxRequests(5);
            auth.setWindowMinutes(1);
            auth.setMessage("Çok fazla deneme. Lütfen daha sonra tekrar deneyin.");
            buckets.add(auth);

            BucketConfig payment = new BucketConfig();
            payment.setName("payment");
            payment.setPathPrefix("/v1/payment/");
            payment.setMaxRequests(10);
            payment.setWindowMinutes(1);
            payment.setMessage("Ödeme isteği limiti aşıldı. Lütfen kısa süre sonra tekrar deneyin.");
            buckets.add(payment);
        }
    }
}
