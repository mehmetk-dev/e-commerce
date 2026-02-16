package com.mehmetkerem.controller;

import com.mehmetkerem.dto.response.WishlistResponse;
import com.mehmetkerem.util.ResultData;

public interface IRestWishlistController {
    ResultData<WishlistResponse> getWishlist();

    ResultData<WishlistResponse> addItem(Long productId);

    ResultData<String> removeItem(Long productId);

    ResultData<String> clearWishlist();
}
