package com.mehmetkerem.model;

import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @Field("order_id")
    private String id;

    @Field("user_id")
    private String userId;

    @Field("order_date")
    private LocalDateTime orderDate;

    @Field("order_status")
    private OrderStatus orderStatus;

    @Field("order_items")
    private List<OrderItem> orderItems;

    @Field("shipping_address")
    private Address shippingAddress;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("payment_status")
    private PaymentStatus paymentStatus;
}
