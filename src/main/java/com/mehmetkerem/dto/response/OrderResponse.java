package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private String id;

    private UserResponse user;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItems;

    private AddressResponse shippingAddress;

    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;

}
