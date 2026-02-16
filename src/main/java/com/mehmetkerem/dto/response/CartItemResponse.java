package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    private Long id;

    private ProductResponse product;

    private int quantity;

    private BigDecimal price;

    private BigDecimal total;
}
