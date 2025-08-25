package com.mehmetkerem.dto.request;

import com.mehmetkerem.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    private String name;
    @Email
    private String email;
    @Size(min=6)
    private String password;
    private Role role;
}
