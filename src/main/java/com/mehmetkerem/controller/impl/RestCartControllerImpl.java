package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestCartController;
import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.service.ICartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/v1/cart")
public class RestCartControllerImpl implements IRestCartController {

    private final ICartService cartService;

    public RestCartControllerImpl(ICartService cartService) {
        this.cartService = cartService;
    }


    @PostMapping("/{userId}/save")
    public ResponseEntity<CartResponse> saveCart(@PathVariable("userId") String userId, @RequestBody List<CartItemRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.saveCart(userId, request));
    }

    @GetMapping("/{userId}")
    @Override
    public ResponseEntity<CartResponse> getCartByUserId(@PathVariable("userId") String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getCartResponseByUserId(userId));
    }

    @PostMapping("/{userId}/items")
    @Override
    public ResponseEntity<CartResponse> addItem(@PathVariable("userId") String userId, @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/items/{productId}")
    @Override
    public ResponseEntity<CartResponse> updateItemQuantity(@PathVariable("userId") String userId, @PathVariable("productId") String productId, @RequestParam int quantity) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.updateItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @Override
    public ResponseEntity<CartResponse> removeItem(@PathVariable("userId") String userId, @PathVariable("productId") String productId) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/{userId}/clear")
    @Override
    public ResponseEntity<String> clearCart(@PathVariable("userId") String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.clearCart(userId));
    }

    @GetMapping("/{userId}/total")
    @Override
    public ResponseEntity<BigDecimal> calculateTotal(@PathVariable("userId") String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(cartService.calculateTotal(userId));
    }


}
