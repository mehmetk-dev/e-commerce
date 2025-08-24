package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestOrderController;
import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.service.IOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/order")
public class RestOrderControllerImpl implements IRestOrderController {

    private final IOrderService orderService;

    public RestOrderControllerImpl(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestParam String userId, @RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(userId,request));
    }
}
