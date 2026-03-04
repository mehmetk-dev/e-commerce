package com.mehmetkerem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long wishlistId;
}
