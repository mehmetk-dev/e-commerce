package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartItemResponse;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.ExceptionMessages;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CartItemMapper;
import com.mehmetkerem.mapper.CartMapper;
import com.mehmetkerem.model.Cart;
import com.mehmetkerem.model.CartItem;
import com.mehmetkerem.repository.CartRepository;
import com.mehmetkerem.service.ICartService;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final IUserService userService;
    private final IProductService productService;

    public CartServiceImpl(CartRepository cartRepository,
                           CartMapper cartMapper,
                           CartItemMapper cartItemMapper,
                           IUserService userService,
                           IProductService productService) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.userService = userService;
        this.productService = productService;
    }

    @Transactional
    public CartResponse saveCart(String userId, List<CartItemRequest> cartItemRequests) {

        validateStock(cartItemRequests);

        Cart cart = cartRepository.findById(userId)
                .orElse(Cart.builder()
                        .userId(userService.getUserById(userId).getId())
                        .items(new ArrayList<>())
                        .build());

        List<CartItem> entityCartItem = cartItemMapper.toEntityCartItem(cartItemRequests);

        cart.setItems(entityCartItem);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        CartResponse response = cartMapper.toResponse(savedCart);
        response.setItems(toResponseCartItem(savedCart.getItems()));

        return response;
    }

    private List<CartItemResponse> toResponseCartItem(List<CartItem> cartItems) {

        List<String> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductResponse> products = productService.getProductsByIds(productIds);

        Map<String, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        return cartItems.stream()
                .map(cartItem -> {
                    ProductResponse product = productMap.get(cartItem.getProductId());
                    if (product == null) {
                        throw new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND);
                    }
                    return cartItemMapper.toResponseWithProduct(cartItem, product);
                })
                .toList();
    }

    private void validateStock(List<CartItemRequest> requests) {
        if (requests == null || requests.isEmpty()) return;

        Map<String, Integer> wanted = new LinkedHashMap<>();
        for (CartItemRequest req : requests) {
            if (req == null || req.getProductId() == null) {
                throw new BadRequestException(ExceptionMessages.PRODUCT_NOT_FOUND);
            }
            Integer q = req.getQuantity();
            if (q == null || q <= 0) {

                throw new BadRequestException(String.format(
                        ExceptionMessages.INSUFFICIENT_STOCK, productService.getProductById(req.getProductId()).getTitle()));
            }
            wanted.merge(req.getProductId(), q, Integer::sum);
        }

        List<String> ids = new ArrayList<>(wanted.keySet());
        List<ProductResponse> products = productService.getProductsByIds(ids);

        Set<String> foundIds = products.stream()
                .map(ProductResponse::getId)
                .collect(Collectors.toSet());

        List<String> missing = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missing.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionMessages.SOME_PRODUCTS_NOT_FOUND, String.join(", ", missing)));
        }

        Map<String, ProductResponse> map = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        for (var entry : wanted.entrySet()) {
            String pid = entry.getKey();
            int requestedQty = entry.getValue();
            ProductResponse p = map.get(pid);

            if (requestedQty > (p.getStock() == null ? 0 : p.getStock())) {
                throw new BadRequestException(
                        String.format(ExceptionMessages.INSUFFICIENT_STOCK, p.getTitle())
                );
            }
        }
    }
}
