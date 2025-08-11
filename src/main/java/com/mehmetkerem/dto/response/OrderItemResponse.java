package com.mehmetkerem.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {

    private String id;

    private ProductResponse product;

    private String title;

    private Integer quantity;

    private BigDecimal price;
}
