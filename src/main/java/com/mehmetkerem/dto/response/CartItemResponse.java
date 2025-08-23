package com.mehmetkerem.dto.response;

import com.mehmetkerem.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {

    private ProductResponse product;

    private int quantity;

    private BigDecimal price;

    private BigDecimal total;

}
