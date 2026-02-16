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
        return ResultHelper.cursor(orderService.getOrdersByUser(requireCurrentUserId(), pageable));
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
}
