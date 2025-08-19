package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartItemResponse;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.mapper.CartItemMapper;
import com.mehmetkerem.mapper.CartMapper;
import com.mehmetkerem.model.Cart;
import com.mehmetkerem.model.CartItem;
import com.mehmetkerem.repository.CartRepository;
import com.mehmetkerem.service.ICartService;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.service.IUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final IUserService userService;
    private final IProductService productService;

    public CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper, CartItemMapper cartItemMapper, IUserService userService, IProductService productService) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.userService = userService;
        this.productService = productService;
    }

    public CartResponse saveCart(String userId, List<CartItemRequest> cartItemRequests) {

        userService.getUserById(userId);

        Cart cart = cartRepository.findById(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());

        List<CartItem> entityCartItem = cartItemMapper.toEntityCartItem(cartItemRequests);

        cart.setItems(entityCartItem);
        cart.setUpdatedAt(LocalDateTime.now());

        CartResponse response = cartMapper.toResponse(cartRepository.save(cart));
        response.setItems(toResponseCartItem(entityCartItem));

        return response;
    }

    private List<CartItemResponse> toResponseCartItem(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItemMapper.toResponseWithProduct
                        (cartItem, productService.getProductResponseById(cartItem.getProductId())))
                .toList();
    }
}
