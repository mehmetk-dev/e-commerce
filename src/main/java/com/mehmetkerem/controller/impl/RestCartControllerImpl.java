package com.mehmetkerem.controller.impl;

import com.mehmetkerem.dto.request.CartItemRequest;
import jakarta.validation.Valid;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.service.ICartService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.ResultHelper;
import com.mehmetkerem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class RestCartControllerImpl {

    private final ICartService cartService;

    private static long requireCurrentUserId() {
        Long id = SecurityUtils.getCurrentUserId();
        if (id == null) {
            throw new InsufficientAuthenticationException("Oturum gerekli");
        }
        return id;
    }

    @GetMapping
    public ResultData<CartResponse> getCart() {
        return ResultHelper.success(cartService.getCartResponseByUserId(requireCurrentUserId()));
    }

    @PostMapping("/items")
    public ResultData<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResultHelper.success(cartService.addItem(requireCurrentUserId(), request));
    }

    @PostMapping("/sync")
    public ResultData<CartResponse> syncCart(@Valid @RequestBody List<CartItemRequest> requests) {
        return ResultHelper.success(cartService.saveCart(requireCurrentUserId(), requests));
    }

    @PutMapping("/items/{productId}")
    public ResultData<CartResponse> updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResultHelper.success(cartService.updateItemQuantity(requireCurrentUserId(), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResultData<CartResponse> removeItem(@PathVariable Long productId) {
        return ResultHelper.success(cartService.removeItem(requireCurrentUserId(), productId));
    }

    @DeleteMapping
    public ResultData<String> clearCart() {
        return ResultHelper.success(cartService.clearCart(requireCurrentUserId()));
    }

    @GetMapping("/total")
    public ResultData<BigDecimal> getTotal() {
        return ResultHelper.success(cartService.calculateTotal(requireCurrentUserId()));
    }

    @PostMapping("/coupon/{code}")
    public ResultData<CartResponse> applyCoupon(@PathVariable String code) {
        return ResultHelper.success(cartService.applyCoupon(requireCurrentUserId(), code));
    }

    @DeleteMapping("/coupon")
    public ResultData<CartResponse> removeCoupon() {
        return ResultHelper.success(cartService.removeCoupon(requireCurrentUserId()));
    }
}
