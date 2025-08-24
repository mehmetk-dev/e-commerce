package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.enums.OrderStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestOrderController {

    ResponseEntity<OrderResponse> saveOrder(String userId, OrderRequest request);

    ResponseEntity<OrderResponse> getOrderResponseById(String orderId);

    ResponseEntity<List<OrderResponse>> getOrdersByUser(String userId);

    ResponseEntity<List<OrderResponse>> getAllOrders();

    ResponseEntity<OrderResponse> updateOrderStatus(String orderId, OrderStatus newStatus);

    ResponseEntity<String> deleteOrder(String orderId);
}
