package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String id;
    private OrderResponse order;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paidAt;
}
