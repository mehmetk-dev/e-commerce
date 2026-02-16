package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Ürün ID boş olamaz.")
    private Long productId;

    @NotNull(message = "Kullanıcı ID boş olamaz.")
    private Long userId;

    @NotBlank(message = "Yorum boş olamaz.")
    @Size(max = 2000, message = "Yorum 2000 karakteri geçemez.")
    private String comment;

    @NotNull(message = "Puan boş olamaz.")
    @DecimalMin(value = "1.0", inclusive = true, message = "Puan en az 1 olabilir.")
    @DecimalMax(value = "5.0", inclusive = true, message = "Puan en fazla 5 olabilir.")
    private Double rating;
}
