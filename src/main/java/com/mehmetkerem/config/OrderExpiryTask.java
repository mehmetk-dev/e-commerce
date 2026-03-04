package com.mehmetkerem.config;

import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.model.Order;
import com.mehmetkerem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ödenmemiş (PENDING) siparişleri belirli süre sonra otomatik iptal eder
 * ve bloklanmış stokları serbest bırakır.
 *
 * Ayarlar application.properties'ten:
 * app.order.expiry-hours=24
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryTask {

    private final OrderRepository orderRepository;

    @Value("${app.order.expiry-hours:24}")
    private int expiryHours;

    /**
     * Her 30 dakikada bir çalışır. PENDING durumunda olan ve
     * oluşturulma tarihinden itibaren expiryHours saat geçmiş siparişleri
     * CANCELLED olarak işaretler.
     */
    @Scheduled(fixedRate = 1_800_000) // 30 dakika
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(expiryHours);
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(cutoff);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Süresi dolmuş sipariş iptal edildi: orderId={}, orderDate={}",
                    order.getId(), order.getOrderDate());
        }

        log.info("Toplam {} adet süresi dolmuş sipariş iptal edildi.", expiredOrders.size());
    }
}
