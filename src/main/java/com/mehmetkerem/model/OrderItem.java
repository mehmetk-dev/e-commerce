package com.mehmetkerem.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItem {

    private String productId;

    private String title;

    private int quantity;

    private BigDecimal price;
}
