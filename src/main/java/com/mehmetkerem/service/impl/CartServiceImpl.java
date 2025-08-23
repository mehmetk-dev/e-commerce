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
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.CartRepository;
import com.mehmetkerem.service.ICartService;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.service.IUserService;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    public Cart getCartByUserId(String userId) {
        return cartRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(ExceptionMessages.NOT_FOUND,userId,"sepet")));
    }

    @Override
    public CartResponse getCartResponseByUserId(String userId) {
        return toResponse(getCartByUserId(userId));
    }

    @Transactional
    @Override
    public CartResponse addItem(String userId, CartItemRequest request) {
        Cart cart = getCartByUserId(userId);

        Product product = productService.getProductById(request.getProductId());
        validateStock(request.getQuantity(),product);
        CartItem item = cartItemMapper.toEntity(request);
        item.setPrice(product.getPrice());
        cart.getItems().add(item);

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getCartByUserId(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProductId(),productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND_IN_CART));

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);

        return CartResponse.builder().items(toResponseCartItem(cart.getItems())).id(userId).build();
    }

    @Override
    public CartResponse removeItem(String userId, String productId) {

        Cart cart = getCartByUserId(userId);
        boolean isRemoved = cart.getItems().removeIf(item -> Objects.equals(item.getProductId(), productId));

        if (!isRemoved){
            throw new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND);
        }
        return CartResponse.builder()
                .items(toResponseCartItem(cart.getItems())).id(userId).build();
    }

    @Override
    public String clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return String.format(Messages.CLEAR_VALUE,userId,"sepet");
    }

    @Override
    public BigDecimal calculateTotal(String userId) {
        return getCartByUserId(userId).getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

    }

    private void validateStock(Integer quantity, Product product){
        if (quantity == null ||  quantity <= 0){
            throw new BadRequestException(ExceptionMessages.UNKNOW_STOCK);
        }
    }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getUserId())
                .items(toResponseCartItem(cart.getItems()))
                .build();
    }

    private List<String> getProductIdsByCartItems(List<CartItem> cartItems){
        return cartItems.stream()
                .map(CartItem::getProductId)
                .toList();
    }



    private List<CartItemResponse> toResponseCartItem(List<CartItem> cartItems) {

        List<ProductResponse> products = productService.getProductsByIds(getProductIdsByCartItems(cartItems));

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
