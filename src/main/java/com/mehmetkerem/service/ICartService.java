package com.mehmetkerem.service;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public interface ICartService {

    CartResponse saveCart(Long userId, List<CartItemRequest> cartItemRequests);

    Cart getCartByUserId(Long userId);

    CartResponse getCartResponseByUserId(Long userId);

    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateItemQuantity(Long userId, Long productId, int quantity);

    CartResponse removeItem(Long userId, Long productId);

    String clearCart(Long userId);

    BigDecimal calculateTotal(Long userId);

    CartResponse applyCoupon(Long userId, String couponCode);

    CartResponse removeCoupon(Long userId);
}
