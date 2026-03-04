package com.mehmetkerem.model;

import com.mehmetkerem.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kullanıcı aktivite logları — admin panelinde müşteri hareketlerini izlemek
 * için.
 * <p>
 * Her kullanıcı eylemi (sipariş, sepet, ödeme, yorum vb.) burada kaydedilir.
 * </p>
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user_id", columnList = "user_id"),
        @Index(name = "idx_activity_type", columnList = "activity_type"),
        @Index(name = "idx_activity_created_at", columnList = "created_at"),
        @Index(name = "idx_activity_user_type", columnList = "user_id, activity_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Eylemi yapan kullanıcı */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Kullanıcı e-postası — silinen kullanıcılar için log'da kalması adına
     * denormalize
     */
    @Column(name = "user_email")
    private String userEmail;

    /** Kullanıcının görünen adı */
    @Column(name = "user_name", length = 100)
    private String userName;

    /** Aktivite türü */
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    /** Etkilenen kaynak türü (Order, Product, Cart vb.) */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /** Etkilenen kaynağın ID'si */
    @Column(name = "entity_id")
    private Long entityId;

    /** Detaylı açıklama — admin için okunabilir mesaj */
    @Column(name = "description", length = 500)
    private String description;

    /** JSON formatında ek bilgi (fiyat, stok, eski/yeni durum vb.) */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /** Kullanıcının IP adresi */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Kullanıcının User-Agent bilgisi */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** Oluşturulma zamanı */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
