package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {

    OrderResponse saveOrder(Long userId, OrderRequest request);

    Order getOrderById(Long orderId);

    OrderResponse getOrderResponseById(Long orderId);

    List<OrderResponse> getOrdersByUser(Long userId);

    Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable);

    List<OrderResponse> getAllOrders();

    Page<OrderResponse> getAllOrders(Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    void updatePaymentStatus(Long orderId, com.mehmetkerem.enums.PaymentStatus newStatus);

    String deleteOrder(Long orderId);

    OrderResponse updateOrderTracking(Long orderId, String trackingNumber, String carrierName);

    /** İade onayında sipariş stoklarını iade etmek için. */
    void revertStockForOrder(Long orderId);

    /** Sipariş fişi/fatura bilgisi (yazdırma veya PDF için). */
    OrderInvoiceResponse getOrderInvoice(Long orderId);
}
