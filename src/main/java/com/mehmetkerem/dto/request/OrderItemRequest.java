package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotBlank(message = "Ürün ID'si boş olamaz.")
    private String productId;

    @NotBlank(message = "Ürün başlığı boş olamaz.")
    private String title;

    @NotNull(message = "Adet bilgisi boş olamaz.")
    @Min(value = 1, message = "Adet en az 1 olmalıdır.")
    private Integer quantity;

    @NotNull(message = "Fiyat boş olamaz.")
    @Positive(message = "Fiyat 0'dan büyük olmalıdır.")
    private BigDecimal price;
}
