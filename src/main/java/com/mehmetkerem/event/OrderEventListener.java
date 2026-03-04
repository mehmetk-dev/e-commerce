package com.mehmetkerem.event;

import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.service.INotificationService;
import com.mehmetkerem.service.impl.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sipariş olaylarını Transaction commit'lendikten SONRA dinler.
 * E-posta ve bildirim gönderimini burada yapar — rollback durumunda
 * hiçbir dış servis çağrılmaz.
 *
 * <p>
 * SRP: OrderServiceImpl'den bildirim sorumluluğu tamamen ayrıldı.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final INotificationService notificationService;
    private final InAppNotificationService inAppNotificationService;

    @Value("${app.admin.user-id:1}")
    private long adminUserId;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEvent(OrderEvent event) {
        switch (event.getType()) {
            case ORDER_CREATED -> handleOrderCreated(event);
            case STATUS_UPDATED -> handleStatusUpdated(event);
            case TRACKING_UPDATED -> handleTrackingUpdated(event);
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        // Sipariş onay e-postası
        sendEmailSafely(() -> notificationService.sendOrderConfirmation(event.getUserEmail(), event.getOrderCode()));

        // Düşük stok uyarıları
        if (event.getStockAlerts() != null) {
            for (OrderEvent.StockAlertInfo alert : event.getStockAlerts()) {
                sendEmailSafely(
                        () -> notificationService.sendStockAlert(alert.getProductTitle(), alert.getRemainingStock()));

                sendInAppSafely(() -> inAppNotificationService.create(
                        adminUserId,
                        "Stok Uyarısı: " + alert.getProductTitle(),
                        alert.getProductTitle() + " ürününün stoku " + alert.getRemainingStock() + " adede düştü!",
                        "STOCK_ALERT",
                        alert.getProductId()));
            }
        }
    }

    private void handleStatusUpdated(OrderEvent event) {
        String statusLabel = resolveStatusLabel(event.getNewStatus());

        // E-posta bildirimi
        sendEmailSafely(() -> notificationService.sendOrderStatusUpdate(
                event.getUserEmail(), event.getOrderCode(), statusLabel));

        // In-app bildirim
        sendInAppSafely(() -> inAppNotificationService.create(
                event.getUserId(),
                "Sipariş durumu: " + statusLabel,
                event.getOrderCode() + " numaralı siparişinizin durumu \"" + statusLabel + "\" olarak güncellendi.",
                "ORDER_" + event.getNewStatus().name(),
                event.getOrderId()));
    }

    private void handleTrackingUpdated(OrderEvent event) {
        // Kargo takip e-postası
        sendEmailSafely(() -> notificationService.sendOrderTrackingEmail(
                event.getUserEmail(), event.getOrderCode(),
                event.getTrackingNumber(), event.getCarrierName()));

        // In-app bildirim
        sendInAppSafely(() -> inAppNotificationService.create(
                event.getUserId(),
                "Siparişiniz kargoya verildi",
                event.getOrderCode() + " numaralı siparişiniz " + event.getCarrierName()
                        + " kargo ile yola çıktı. Takip no: " + event.getTrackingNumber(),
                "ORDER_SHIPPED",
                event.getOrderId()));
    }

    /**
     * OrderStatus → Türkçe etiket dönüşümü — tek noktada tanımlı.
     */
    public static String resolveStatusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Beklemede";
            case PAID -> "Ödendi";
            case SHIPPED -> "Kargoya Verildi";
            case DELIVERED -> "Teslim Edildi";
            case CANCELLED -> "İptal Edildi";
        };
    }

    private void sendEmailSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("E-posta gönderilemedi: {}", e.getMessage());
        }
    }

    private void sendInAppSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("In-app bildirim oluşturulamadı: {}", e.getMessage());
        }
    }
}
