package com.mehmetkerem.event;

import com.mehmetkerem.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Sipariş ile ilgili olayları temsil eden event nesnesi.
 * Transaction commit'lendikten SONRA dinlenir —
 * e-posta / bildirim gibi dış servis çağrıları güvenli biçimde yapılır.
 */
@Getter
@Builder
public class OrderEvent {

    private final OrderEventType type;
    private final Long orderId;
    private final Long userId;
    private final String userEmail;
    private final String orderCode;

    // Status update
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    // Tracking
    private final String trackingNumber;
    private final String carrierName;

    // Low stock alerts
    private final List<StockAlertInfo> stockAlerts;

    @Getter
    @Builder
    public static class StockAlertInfo {
        private final String productTitle;
        private final int remainingStock;
        private final Long productId;
    }
}
