package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mehmetkerem.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    private ProductResponse product;

    private int quantity;

    private BigDecimal price;

    private BigDecimal total;

}
