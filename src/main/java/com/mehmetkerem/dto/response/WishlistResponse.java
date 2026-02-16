package com.mehmetkerem.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistResponse {

    private Long id;
    private UserResponse user;
    private List<WishlistItemResponse> items;

}
