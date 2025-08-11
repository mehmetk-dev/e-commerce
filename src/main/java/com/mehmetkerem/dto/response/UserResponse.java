package com.mehmetkerem.dto.response;

import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.Address;
import com.mehmetkerem.model.CartItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private List<AddressResponse> addresses;
    private LocalDateTime createdAt;
}
