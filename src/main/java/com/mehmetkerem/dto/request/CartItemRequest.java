package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull(message = "Ürün ID boş olamaz.")
    private Long productId;

    @NotNull(message = "Miktar boş olamaz.")
    @Min(value = 1, message = "Miktar en az 1 olmalıdır.")
    private Integer quantity;
}
