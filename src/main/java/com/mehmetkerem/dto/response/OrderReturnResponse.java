package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.ReturnStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderReturnResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private ReturnStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}