package com.mehmetkerem.controller.impl;

import com.mehmetkerem.controller.IRestCartController;
import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.service.ICartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/v1/cart")
public class RestCartControllerImpl implements IRestCartController {

    private final ICartService cartService;

    public RestCartControllerImpl(ICartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<CartResponse> saveCart(@PathVariable String userId,@RequestBody List<CartItemRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.saveCart(userId,request));
    }
}
