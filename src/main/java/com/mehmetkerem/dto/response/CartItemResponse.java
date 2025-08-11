package com.mehmetkerem.dto.response;

import com.mehmetkerem.model.Product;
import lombok.Data;

@Data
public class CartItemResponse {

    private ProductResponse product;

    private int quantity;

}
