package com.mehmetkerem.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mehmetkerem.enums.OrderStatus;
import com.mehmetkerem.enums.PaymentStatus;
import com.mehmetkerem.model.OrderItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Sipariş tarihi boş olamaz.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime orderDate;

    @NotNull(message = "Sipariş durumu boş olamaz.")
    private OrderStatus orderStatus;

    @NotEmpty(message = "Sipariş kalemleri boş olamaz.")
    private List<OrderItemRequest> orderItems;

    @NotEmpty(message = "Adres ID boş olamaz.")
    private String addressId;

    @NotNull(message = "Toplam tutar boş olamaz.")
    private BigDecimal totalAmount;

    @NotNull(message = "Ödeme durumu boş olamaz.")
    private PaymentStatus paymentStatus;
}
