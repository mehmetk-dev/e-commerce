package com.mehmetkerem.dto.request;

import com.mehmetkerem.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserRequest {

    @NotBlank(message = "İsim boş olamaz.")
    @Size(max = 255, message = "İsim 255 karakteri geçemez.")
    private String name;

    @NotBlank(message = "E-posta boş olamaz.")
    @Email(message = "Geçerli bir e-posta giriniz.")
    private String email;

    @NotBlank(message = "Parola boş olamaz.")
    @Size(min = 8, message = "Parola en az 8 karakter olmalı.")
    private String password;

    @NotNull(message = "Kullanıcı rolü boş olamaz.")
    private Role role;

    private List<Long> addressIds;
}
