package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "İsim boş olamaz.")
    @Size(max = 255, message = "İsim 255 karakteri geçemez.")
    private String name;
}
