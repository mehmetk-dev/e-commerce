package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull
    private String productId;

    @NotNull
    private Integer quantity;
}
