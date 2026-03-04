package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.OrderStatusHistoryResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    OrderResponse saveOrder(Long userId, OrderRequest request);

    Order getOrderById(Long orderId);

    OrderResponse getOrderResponseById(Long orderId);

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Admin sipariş filtreleme: status, paymentStatus, userId, tarih aralığı, arama
     */
    Page<OrderResponse> searchOrders(OrderStatus status, PaymentStatus paymentStatus,
            Long userId, LocalDateTime from, LocalDateTime to, String query, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    void updatePaymentStatus(Long orderId, PaymentStatus newStatus);

    String deleteOrder(Long orderId);

    OrderResponse updateOrderTracking(Long orderId, String trackingNumber, String carrierName);

    /** Kullanıcı sipariş iptali. */
    OrderResponse cancelOrder(Long orderId, Long userId);

    /** İade onayında sipariş stoklarını iade etmek için. */
    void revertStockForOrder(Long orderId);

    /** Sipariş fişi/fatura bilgisi (yazdırma veya PDF için). */
    OrderInvoiceResponse getOrderInvoice(Long orderId);

    /** Sipariş durum geçmişi (timeline). */
    List<OrderStatusHistoryResponse> getOrderTimeline(Long orderId);
}
