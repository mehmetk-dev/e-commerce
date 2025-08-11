// ReviewRequest.java (create)
package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotBlank(message = "Ürün ID boş olamaz.")
    private String productId;

    @NotBlank(message = "Kullanıcı ID boş olamaz.")
    private String userId;

    @NotBlank(message = "Yorum boş olamaz.")
    @Size(max = 2000, message = "Yorum 2000 karakteri geçemez.")
    private String comment;

    @NotNull(message = "Puan boş olamaz.")
    @Min(value = 1, message = "Puan en az 1 olmalı.")
    @Max(value = 5, message = "Puan en fazla 5 olabilir.")
    private Integer rating;
}
