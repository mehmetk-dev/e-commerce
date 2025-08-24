package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.OrderRequest;
import com.mehmetkerem.dto.response.OrderResponse;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.model.Order;

import java.util.List;

public interface IOrderService {

    OrderResponse saveOrder(String userId, OrderRequest request);

    Order getOrderById(String orderId);

    OrderResponse getOrderResponseById(String orderId);

    List<OrderResponse> getOrdersByUser(String userId);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus);

    String deleteOrder(String orderId);
}
