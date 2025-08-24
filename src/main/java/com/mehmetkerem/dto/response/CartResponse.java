package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private String id;
    private List<CartItemResponse> items;
    private LocalDateTime updatedAt;
}
