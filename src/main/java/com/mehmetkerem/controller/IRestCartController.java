package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IRestCartController {
    ResponseEntity<CartResponse> saveCart(Long userId, List<CartItemRequest> request);

    ResponseEntity<CartResponse> getCartByUserId(Long userId);

    ResponseEntity<CartResponse> addItem(Long userId, CartItemRequest request);

    ResponseEntity<CartResponse> updateItemQuantity(Long userId, Long productId, int quantity);

    ResponseEntity<CartResponse> removeItem(Long userId, Long productId);

    ResponseEntity<String> clearCart(Long userId);

    ResponseEntity<BigDecimal> calculateTotal(Long userId);
}
