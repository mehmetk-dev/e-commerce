package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {

    private Long id;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private Long changedBy;
    private String note;
    private LocalDateTime changedAt;
}
