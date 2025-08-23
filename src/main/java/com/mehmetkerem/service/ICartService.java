package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;

import java.util.List;

public interface ICartService {

    CartResponse saveCart(String userId, List<CartItemRequest> cartItemRequests);
    CartResponse getCartByUserId(String userId);
    CartResponse addItem(String userId, CartItemRequest request);
    CartResponse updateItemQuantity(String userId, String productId, int quantity);
    CartResponse removeItem(String userId, String productId);
    String clearCart(String userId);
    Double calculateTotal(String userId);
}
