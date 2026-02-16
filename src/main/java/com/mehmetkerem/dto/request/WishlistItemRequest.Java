package com.mehmetkerem.dto.request;

import lombok.Data;

@Data
public class WishlistItemRequest {
    
    @NotNull
    private Long productId;

    @NotNull
    private Long wishlistId;
}
