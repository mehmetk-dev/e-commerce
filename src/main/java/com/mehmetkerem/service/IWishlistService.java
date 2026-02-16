package com.mehmetkerem.service;

import com.mehmetkerem.dto.response.WishlistResponse;

public interface IWishlistService {
    WishlistResponse getWishlistByUserId(Long userId);

    WishlistResponse addItemToWishlist(Long userId, Long productId);

    void removeItemFromWishlist(Long userId, Long productId);

    void clearWishlist(Long userId);
}
