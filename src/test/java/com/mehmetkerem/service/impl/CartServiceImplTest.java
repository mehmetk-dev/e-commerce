package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartItemResponse;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.dto.response.ProductResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.CartItemMapper;
import com.mehmetkerem.mapper.CartMapper;
import com.mehmetkerem.model.Cart;
import com.mehmetkerem.model.CartItem;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.repository.CartRepository;
import com.mehmetkerem.service.ICouponService;
import com.mehmetkerem.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private IProductService productService;

    @Mock
    private ICouponService couponService;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    private Cart cart;
    private Product product;
    private ProductResponse productResponse;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .items(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(PRODUCT_ID)
                .title("Antika Saat")
                .price(new BigDecimal("100"))
                .stock(5)
                .build();

        productResponse = ProductResponse.builder()
                .id(PRODUCT_ID)
                .title("Antika Saat")
                .price(new BigDecimal("100"))
                .stock(5)
                .build();

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(PRODUCT_ID);
        cartItemRequest.setQuantity(2);
    }

    @Test
    @DisplayName("getCartByUserId - mevcut sepet döner")
    void getCartByUserId_WhenCartExists_ShouldReturnCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(USER_ID, result.getUserId());
        verify(cartRepository).findByUserId(USER_ID);
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCartByUserId - sepet yoksa yeni sepet oluşturulur")
    void getCartByUserId_WhenCartNotExists_ShouldCreateAndReturnNewCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        Cart result = cartService.getCartByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("addItem - sepete ürün eklenir")
    void addItem_WhenValid_ShouldAddItemAndReturnResponse() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);
        CartItem newItem = CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .price(new BigDecimal("100"))
                .build();
        when(cartItemMapper.toEntity(cartItemRequest)).thenReturn(newItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productService.getProductResponsesByIds(List.of(PRODUCT_ID))).thenReturn(List.of(productResponse));

        CartResponse response = new CartResponse();
        response.setUserId(USER_ID);
        response.setItems(List.of(new CartItemResponse()));
        when(cartItemMapper.toResponseWithProduct(any(), any())).thenReturn(new CartItemResponse());

        CartResponse result = cartService.addItem(USER_ID, cartItemRequest);

        assertNotNull(result);
        verify(productService).getProductById(PRODUCT_ID);
        verify(cartRepository).save(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("addItem - stok yetersizse BadRequestException")
    void addItem_WhenInsufficientStock_ShouldThrowBadRequestException() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        product.setStock(1);
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);
        cartItemRequest.setQuantity(10);

        assertThrows(BadRequestException.class, () -> cartService.addItem(USER_ID, cartItemRequest));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeItem - ürün sepetten çıkarılır")
    void removeItem_WhenItemExists_ShouldRemoveAndReturnResponse() {
        CartItem item = CartItem.builder().productId(PRODUCT_ID).quantity(2).price(new BigDecimal("100")).build();
        cart.getItems().add(item);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse result = cartService.removeItem(USER_ID, PRODUCT_ID);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("removeItem - sepette olmayan ürün için NotFoundException")
    void removeItem_WhenItemNotInCart_ShouldThrowNotFoundException() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        assertTrue(cart.getItems().isEmpty());

        assertThrows(NotFoundException.class, () -> cartService.removeItem(USER_ID, PRODUCT_ID));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("clearCart - sepet temizlenir")
    void clearCart_ShouldClearItemsAndReturnMessage() {
        cart.getItems().add(CartItem.builder().productId(PRODUCT_ID).quantity(1).price(BigDecimal.ONE).build());
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.clearCart(USER_ID);

        assertTrue(result.contains(USER_ID.toString()));
        assertTrue(result.contains("sepet"));
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("getCartResponseByUserId - response döner")
    void getCartResponseByUserId_ShouldReturnCartResponse() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        CartResponse result = cartService.getCartResponseByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    @DisplayName("calculateTotal - boş sepette 0 döner")
    void calculateTotal_WhenCartEmpty_ShouldReturnZero() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        BigDecimal total = cartService.calculateTotal(USER_ID);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    @DisplayName("calculateTotal - ürünlü sepette toplam hesaplanır")
    void calculateTotal_WhenCartHasItems_ShouldReturnSum() {
        cart.getItems().add(CartItem.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .price(new BigDecimal("50"))
                .build());
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        BigDecimal total = cartService.calculateTotal(USER_ID);

        assertEquals(new BigDecimal("100"), total);
    }
}
