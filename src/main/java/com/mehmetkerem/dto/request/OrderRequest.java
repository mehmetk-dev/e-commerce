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

    @NotEmpty(message = "Adres ID boş olamaz.")
    private String addressId;

    @NotNull(message = "Ödeme durumu boş olamaz.")
    private PaymentStatus paymentStatus;

    private String note;
}
