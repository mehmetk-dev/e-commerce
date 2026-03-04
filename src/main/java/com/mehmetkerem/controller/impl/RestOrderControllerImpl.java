package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestOrderController;
import jakarta.validation.Valid;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.model.Order;
import com.mehmetkerem.service.IOrderService;
import com.mehmetkerem.util.SecurityUtils;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/order")
@RequiredArgsConstructor
public class RestOrderControllerImpl implements IRestOrderController {

    private final IOrderService orderService;
    private final com.mehmetkerem.service.IInvoicePdfService invoicePdfService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @Override
    @PutMapping("/{orderId}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<OrderResponse> updateTrackingInfo(
            @PathVariable("orderId") Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrierName) {
        return ResultHelper.success(orderService.updateOrderTracking(orderId, trackingNumber, carrierName));
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<com.mehmetkerem.dto.response.CursorResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, cappedSize, sort);
        return ResultHelper.cursor(orderService.getAllOrders(pageable));
    }

    /**
     * Admin sipariş filtreleme.
     * Opsiyonel parametreler: status, paymentStatus, userId, from, to, q (sipariş
     * kodu)
     */
    @Override
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultData<com.mehmetkerem.dto.response.CursorResponse<OrderResponse>> searchOrders(
            @RequestParam(required = false) com.mehmetkerem.enums.OrderStatus status,
            @RequestParam(required = false) com.mehmetkerem.enums.PaymentStatus paymentStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, cappedSize, sort);
        return ResultHelper.cursor(orderService.searchOrders(status, paymentStatus, userId, from, to, q, pageable));
    }

    @Override
    @GetMapping("/my-orders")
    public ResultData<com.mehmetkerem.dto.response.CursorResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, cappedSize, sort);
        return ResultHelper.cursor(orderService.getOrdersByUserId(requireCurrentUserId(), pageable));
    }

    @Override
    @PostMapping("/save")
    public ResultData<OrderResponse> saveOrder(@Valid @RequestBody com.mehmetkerem.dto.request.OrderRequest request) {
        return ResultHelper.success(orderService.saveOrder(requireCurrentUserId(), request));
    }

    @Override
    @GetMapping("/{orderId}/invoice")
    public ResultData<OrderInvoiceResponse> getOrderInvoice(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Oturum gerekli");
        }
        boolean isOwner = order.getUserId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.getCurrentUser() != null
                && SecurityUtils.getCurrentUser().getRole() == com.mehmetkerem.enums.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BadRequestException("Bu siparişe ait fişi görüntüleme yetkiniz yok.");
        }
        return ResultHelper.success(orderService.getOrderInvoice(orderId));
    }

    @Override
    @PostMapping("/{orderId}/cancel")
    public ResultData<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("Oturum gerekli");
        }
        if (!order.getUserId().equals(currentUserId)) {
            throw new BadRequestException("Bu siparişi iptal etme yetkiniz yok.");
        }
        if (order.getOrderStatus() != com.mehmetkerem.enums.OrderStatus.PENDING
                && order.getOrderStatus() != com.mehmetkerem.enums.OrderStatus.PAID) {
            throw new BadRequestException("Sadece bekleyen veya ödenen siparişler iptal edilebilir.");
        }
        return ResultHelper
                .success(orderService.updateOrderStatus(orderId, com.mehmetkerem.enums.OrderStatus.CANCELLED));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResultData<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        com.mehmetkerem.enums.OrderStatus orderStatus = com.mehmetkerem.enums.OrderStatus.valueOf(status.toUpperCase());
        return ResultHelper.success(orderService.updateOrderStatus(orderId, orderStatus));
    }

    /**
     * Sipariş durum geçmişi (timeline).
     * Sipariş sahibi veya admin görebilir.
     */
    @Override
    @GetMapping("/{orderId}/timeline")
    public ResultData<java.util.List<com.mehmetkerem.dto.response.OrderStatusHistoryResponse>> getOrderTimeline(
            @PathVariable Long orderId) {
        return ResultHelper.success(orderService.getOrderTimeline(orderId));
    }

    @Override
    @GetMapping("/{orderId}/invoice/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(orderId);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=fatura-" + orderId + ".pdf")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }
}
