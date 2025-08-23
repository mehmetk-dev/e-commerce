package com.mehmetkerem.controller;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IRestCartController {
    ResponseEntity<CartResponse> saveCart(String userId, List<CartItemRequest> request);
    ResponseEntity<CartResponse> getCartByUserId(String userId);
    ResponseEntity<CartResponse> addItem(String userId, CartItemRequest request);
    ResponseEntity<CartResponse> updateItemQuantity(String userId, String productId, int quantity);
    ResponseEntity<CartResponse> removeItem(String userId, String productId);
    ResponseEntity<String> clearCart(String userId);
    ResponseEntity<BigDecimal> calculateTotal(String userId);
}
