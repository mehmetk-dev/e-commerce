package com.mehmetkerem.model;

import com.mehmetkerem.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Sipariş durum değişikliklerini loglar.
 * Her durum değişikliğinde yeni bir kayıt oluşturulur → timeline oluşur.
 */
@Entity
@Table(name = "order_status_history", indexes = {
        @Index(name = "idx_osh_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    /** Kim değiştirdi (admin userId veya sistem için null) */
    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "note")
    private String note;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null)
            changedAt = LocalDateTime.now();
    }
}
