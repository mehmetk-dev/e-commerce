package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mehmetkerem.enums.PaymentMethod;
import com.mehmetkerem.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class PaymentResponse {
    private String id;
    private UserResponse user;
    private OrderResponse order;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
}
