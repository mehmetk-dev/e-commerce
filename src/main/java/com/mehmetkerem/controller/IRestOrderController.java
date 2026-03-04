package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.CursorResponse;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.dto.response.OrderStatusHistoryResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.util.ResultData;

import java.time.LocalDateTime;
import java.util.List;

public interface IRestOrderController {
    ResultData<OrderResponse> updateTrackingInfo(Long orderId, String trackingNumber, String carrierName);

    ResultData<CursorResponse<OrderResponse>> getAllOrders(int page, int size, String sortBy, String direction);

    /**
     * Admin sipariş filtreleme: status, paymentStatus, userId, tarih aralığı, arama
     */
    ResultData<CursorResponse<OrderResponse>> searchOrders(
            OrderStatus status, PaymentStatus paymentStatus, Long userId,
            LocalDateTime from, LocalDateTime to, String q,
            int page, int size, String sortBy, String direction);

    ResultData<CursorResponse<OrderResponse>> getMyOrders(int page, int size, String sortBy, String direction);

    ResultData<OrderResponse> saveOrder(com.mehmetkerem.dto.request.OrderRequest request);

    /** Kendi siparişi veya admin için sipariş fişi. */
    ResultData<OrderInvoiceResponse> getOrderInvoice(Long orderId);

    /** Kullanıcı kendi bekleyen siparişini iptal eder. */
    ResultData<OrderResponse> cancelOrder(Long orderId);

    /** Admin sipariş durumu günceller. */
    ResultData<OrderResponse> updateOrderStatus(Long orderId, String status);

    /** Sipariş durum geçmişi (timeline). */
    ResultData<List<OrderStatusHistoryResponse>> getOrderTimeline(Long orderId);

    /** Fatura PDF indirme. */
    org.springframework.http.ResponseEntity<byte[]> downloadInvoicePdf(Long orderId);
}
