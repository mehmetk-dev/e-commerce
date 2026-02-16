package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Eski şifre gerekli")
    private String oldPassword;

    @NotBlank(message = "Yeni şifre gerekli")
    @Size(min = 6, message = "Yeni şifre en az 6 karakter olmalı")
    private String newPassword;
}
