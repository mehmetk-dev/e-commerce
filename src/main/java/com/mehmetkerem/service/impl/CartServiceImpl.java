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
import lombok.extern.slf4j.Slf4j;
import com.mehmetkerem.util.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    private final IProductService productService;
    private final com.mehmetkerem.service.ICouponService couponService;

    public CartServiceImpl(CartRepository cartRepository,
            CartMapper cartMapper,
            CartItemMapper cartItemMapper,
            IProductService productService,
            com.mehmetkerem.service.ICouponService couponService) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productService = productService;
        this.couponService = couponService;
    }

    @Transactional
    @Override
    public CartResponse saveCart(Long userId, List<CartItemRequest> cartItemRequests) {
        log.info("Sepet kaydediliyor. UserId: {}, Ürün Adedi: {}", userId, cartItemRequests.size());
        validateStock(cartItemRequests);

        Cart cart = getCartByUserId(userId);

        List<CartItem> entityCartItem = cartItemMapper.toEntityCartItem(cartItemRequests);

        List<Long> productIds = entityCartItem.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        List<ProductResponse> products = productService.getProductResponsesByIds(productIds);

        Map<Long, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        for (CartItem item : entityCartItem) {
            ProductResponse product = productMap.get(item.getProductId());
            if (product == null) {
                throw new NotFoundException("Product not found: " + item.getProductId());
            }
            item.setPrice(product.getPrice());
        }

        // Merge logic used here
        Map<Long, CartItem> mergedItems = new LinkedHashMap<>();
        for (CartItem item : entityCartItem) {
            if (mergedItems.containsKey(item.getProductId())) {
                CartItem existing = mergedItems.get(item.getProductId());
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            } else {
                mergedItems.put(item.getProductId(), item);
            }
        }

        List<CartItem> finalCartItems = new ArrayList<>(mergedItems.values());

        cart.getItems().clear();
        cart.getItems().addAll(finalCartItems);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        CartResponse response = cartMapper.toResponse(savedCart);
        response.setUserId(userId);
        response.setItems(toResponseCartItem(savedCart.getItems()));

        return response;
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        // Create cart if not exists
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    @Override
    public CartResponse getCartResponseByUserId(Long userId) {
        return toResponse(getCartByUserId(userId));
    }

    @Transactional
    @Override
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getCartByUserId(userId);

        Product product = productService.getProductById(request.getProductId());
        validateStock(request.getQuantity(), product);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = cartItemMapper.toEntity(request);
            newItem.setPrice(product.getPrice());
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        log.info("Sepete ürün eklendi. Kullanıcı ID: {}, Ürün ID: {}, Miktar: {}", userId, request.getProductId(),
                request.getQuantity());

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    @Override
    public CartResponse updateItemQuantity(Long userId, Long productId, int quantity) {

        validateStock(quantity, productService.getProductById(productId));

        Cart cart = getCartByUserId(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND_IN_CART));

        cartItem.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return CartResponse.builder().items(toResponseCartItem(cart.getItems())).userId(userId).build();
    }

    @Override
    public CartResponse removeItem(Long userId, Long productId) {

        Cart cart = getCartByUserId(userId);
        boolean isRemoved = cart.getItems().removeIf(item -> Objects.equals(item.getProductId(), productId));
        cart.setUpdatedAt(LocalDateTime.now());

        if (!isRemoved) {
            log.warn("Sepetten ürün silme hatası: Ürün bulunamadı. Kullanıcı ID: {}, Ürün ID: {}", userId, productId);
            throw new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND);
        }
        log.info("Ürün sepetten çıkarıldı. Kullanıcı ID: {}, Ürün ID: {}", userId, productId);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public String clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return String.format(Messages.CLEAR_VALUE, userId, "sepet");
    }

    @Override
    @Transactional
    public CartResponse applyCoupon(Long userId, String couponCode) {
        Cart cart = getCartByUserId(userId);

        // Calculate current total
        BigDecimal currentTotal = calculateRawTotal(cart);

        // Validate and check if applicable (throws exception if invalid)
        couponService.applyCoupon(couponCode, currentTotal);

        cart.setCouponCode(couponCode);
        cartRepository.save(cart);

        CartResponse response = toResponse(cart);
        // Recalculate total with discount (validating/applying to ensure price is
        // updated if needed)
        couponService.applyCoupon(couponCode, currentTotal);
        return response;
    }

    @Override
    @Transactional
    public CartResponse removeCoupon(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.setCouponCode(null);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    private BigDecimal calculateRawTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotal(Long userId) {
        Cart cart = getCartByUserId(userId);
        BigDecimal total = calculateRawTotal(cart);

        if (cart.getCouponCode() != null) {
            try {
                return couponService.applyCoupon(cart.getCouponCode(), total);
            } catch (Exception e) {
                // If coupon invalid now (expired etc), return raw total or handle error
                // For now, let's log and clear invalid coupon? Or just return raw total.
                return total;
            }
        }
        return total;
    }

    private void validateStock(Integer quantity, Product product) {
        if (quantity == null || quantity <= 0 || product.getStock() < quantity) {
            throw new BadRequestException(ExceptionMessages.UNKNOW_STOCK);
        }
    }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(toResponseCartItem(cart.getItems()))
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private List<Long> getProductIdsByCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
    }

    private List<CartItemResponse> toResponseCartItem(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }
        List<ProductResponse> products = productService.getProductResponsesByIds(getProductIdsByCartItems(cartItems));

        Map<Long, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        return cartItems.stream()
                .map(cartItem -> {
                    ProductResponse product = productMap.get(cartItem.getProductId());
                    // If product removed but still in cart, handle gracefully or throw
                    if (product == null) {
                        // Option: throw new NotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND);
                        // Or create dummy product response
                        return cartItemMapper.toResponseWithProduct(cartItem,
                                ProductResponse.builder().id(cartItem.getProductId()).title("Unknown Product")
                                        .price(cartItem.getPrice()).build());
                    }
                    return cartItemMapper.toResponseWithProduct(cartItem, product);
                })
                .collect(Collectors.toList());
    }

    private void validateStock(List<CartItemRequest> requests) {
        if (requests == null || requests.isEmpty())
            return; // Empty request is valid (clearing cart maybe?) or just ignore

        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (CartItemRequest req : requests) {
            if (req == null || req.getProductId() == null) {
                continue;
            }
            Integer q = req.getQuantity();
            if (q == null || q <= 0) {
                Product p = productService.getProductById(req.getProductId());
                throw new BadRequestException(String.format(
                        ExceptionMessages.INSUFFICIENT_STOCK,
                        p.getTitle()));
            }
            wanted.merge(req.getProductId(), q, (a, b) -> a + b);
        }

        if (wanted.isEmpty())
            return;

        List<Long> ids = new ArrayList<>(wanted.keySet());
        List<ProductResponse> products = productService.getProductResponsesByIds(ids);

        Set<Long> foundIds = products.stream()
                .map(ProductResponse::getId)
                .collect(Collectors.toSet());

        // Check for missing products
        List<Long> missing = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new NotFoundException("Products not found: " + missing);
        }

        Map<Long, ProductResponse> map = products.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        for (var entry : wanted.entrySet()) {
            Long pid = entry.getKey();
            int requestedQty = entry.getValue();
            ProductResponse p = map.get(pid);

            if (requestedQty > (p.getStock() == null ? 0 : p.getStock())) {
                throw new BadRequestException(
                        String.format(ExceptionMessages.INSUFFICIENT_STOCK, p.getTitle()));
            }
        }
    }
}
