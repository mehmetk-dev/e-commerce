package com.mehmetkerem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemResponse {

    private ProductResponse product;

    private String title;

    private Integer quantity;

    private BigDecimal price;
}
