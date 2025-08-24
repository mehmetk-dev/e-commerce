package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestOrderController;
import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.service.IOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/order")
public class RestOrderControllerImpl implements IRestOrderController {

    private final IOrderService orderService;

    public RestOrderControllerImpl(IOrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    @PostMapping("/save")
    public ResponseEntity<OrderResponse> saveOrder(
            @RequestParam String userId, @RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(userId,request));
    }

    @Override
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderResponseById(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(orderService.getOrderResponseById(orderId));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable("orderId") String orderId,
            @RequestParam OrderStatus newStatus
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(orderService.deleteOrder(orderId));
    }
}
