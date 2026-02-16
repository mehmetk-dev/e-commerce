package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderReturnRequest {

    @NotNull(message = "Sipariş ID boş olamaz.")
    private Long orderId;

    private String reason;
}
