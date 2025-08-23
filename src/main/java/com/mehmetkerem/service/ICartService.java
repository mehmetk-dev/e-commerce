package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public interface ICartService {

    CartResponse saveCart(String userId, List<CartItemRequest> cartItemRequests);
    Cart getCartByUserId(String userId);
    CartResponse getCartResponseByUserId(String userId);
    CartResponse addItem(String userId, CartItemRequest request);
    CartResponse updateItemQuantity(String userId, String productId, int quantity);
    CartResponse removeItem(String userId, String productId);
    String clearCart(String userId);
    BigDecimal calculateTotal(String userId);
}
