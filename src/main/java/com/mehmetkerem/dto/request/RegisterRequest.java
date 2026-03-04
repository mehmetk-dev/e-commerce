package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "İsim boş olamaz.")
    private String name;

    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    @NotBlank(message = "E-posta boş olamaz.")
    private String email;

    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır.")
    @NotBlank(message = "Şifre boş olamaz.")
    private String password;
}
