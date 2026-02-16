package com.mehmetkerem.dto.response;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistItemResponse {

    private Long id;
    private ProductResponse product;
    private Long wishlistId;
}
