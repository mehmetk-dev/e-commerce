package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductRequest {

    @NotBlank(message = "Ürün adı boş olamaz.")
    private String title;

    @Size(max = 4000, message = "Açıklama 4000 karakteri geçemez.")
    private String description;

    @NotNull(message = "Fiyat boş olamaz.")
    @Positive(message = "Fiyat 0'dan büyük olmalıdır.")
    private BigDecimal price;

    @NotNull(message = "Stok adedi boş olamaz.")
    @PositiveOrZero(message = "Stok adedi eksi olamaz.")
    private Integer stock;

    @NotNull(message = "Kategori ID boş olamaz.")
    private Long categoryId;

    @NotEmpty(message = "En az bir görsel URL'i gereklidir.")
    private List<@NotBlank(message = "Görsel URL'i boş olamaz.") String> imageUrls;

    private Map<String, Object> attributes;
}
