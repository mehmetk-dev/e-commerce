package com.mehmetkerem.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {

    private String id;
    private List<CartItemResponse> items;
    private LocalDateTime updatedAt;
}
