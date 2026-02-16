package com.mehmetkerem.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartRequest {

    private List<CartItemRequest> items;

    private LocalDateTime updatedAt;
}
