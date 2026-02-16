package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestCartControllerImpl;
import com.mehmetkerem.dto.request.CartItemRequest;
import com.mehmetkerem.dto.response.CartResponse;
import com.mehmetkerem.service.ICartService;
import com.mehmetkerem.util.ResultData;
import com.mehmetkerem.util.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestCartControllerTest {

    @Mock
    private ICartService cartService;

    @InjectMocks
    private RestCartControllerImpl controller;

    private static final Long USER_ID = SecurityTestUtils.DEFAULT_USER_ID;
    private static final Long PRODUCT_ID = 10L;
    private CartResponse cartResponse;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.setCurrentUser();
        cartResponse = CartResponse.builder().id(1L).userId(USER_ID).build();
        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(PRODUCT_ID);
        cartItemRequest.setQuantity(2);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearContext();
    }

    @Test
    @DisplayName("getCart - sepet döner")
    void getCart_ShouldReturnCartResponse() {
        when(cartService.getCartResponseByUserId(USER_ID)).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.getCart();

        assertTrue(result.isStatus());
        assertEquals(USER_ID, result.getData().getUserId());
        verify(cartService).getCartResponseByUserId(USER_ID);
    }

    @Test
    @DisplayName("addItem - ürün eklenir")
    void addItem_ShouldReturnUpdatedCart() {
        when(cartService.addItem(eq(USER_ID), any(CartItemRequest.class))).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.addItem(cartItemRequest);

        assertTrue(result.isStatus());
        verify(cartService).addItem(USER_ID, cartItemRequest);
    }

    @Test
    @DisplayName("syncCart - sepet senkronize edilir")
    void syncCart_ShouldReturnCart() {
        when(cartService.saveCart(eq(USER_ID), anyList())).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.syncCart(List.of(cartItemRequest));

        assertTrue(result.isStatus());
        verify(cartService).saveCart(USER_ID, List.of(cartItemRequest));
    }

    @Test
    @DisplayName("updateQuantity - miktar güncellenir")
    void updateQuantity_ShouldReturnCart() {
        when(cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 3)).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.updateQuantity(PRODUCT_ID, 3);

        assertTrue(result.isStatus());
        verify(cartService).updateItemQuantity(USER_ID, PRODUCT_ID, 3);
    }

    @Test
    @DisplayName("removeItem - ürün çıkarılır")
    void removeItem_ShouldReturnCart() {
        when(cartService.removeItem(USER_ID, PRODUCT_ID)).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.removeItem(PRODUCT_ID);

        assertTrue(result.isStatus());
        verify(cartService).removeItem(USER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("clearCart - sepet temizlenir")
    void clearCart_ShouldReturnMessage() {
        when(cartService.clearCart(USER_ID)).thenReturn("Sepet temizlendi");

        ResultData<String> result = controller.clearCart();

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("temizlendi"));
        verify(cartService).clearCart(USER_ID);
    }

    @Test
    @DisplayName("getTotal - toplam tutar döner")
    void getTotal_ShouldReturnTotal() {
        when(cartService.calculateTotal(USER_ID)).thenReturn(new BigDecimal("99.99"));

        ResultData<BigDecimal> result = controller.getTotal();

        assertTrue(result.isStatus());
        assertEquals(new BigDecimal("99.99"), result.getData());
        verify(cartService).calculateTotal(USER_ID);
    }

    @Test
    @DisplayName("applyCoupon - kupon uygulanır")
    void applyCoupon_ShouldReturnCart() {
        when(cartService.applyCoupon(USER_ID, "INDIRIM10")).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.applyCoupon("INDIRIM10");

        assertTrue(result.isStatus());
        verify(cartService).applyCoupon(USER_ID, "INDIRIM10");
    }

    @Test
    @DisplayName("removeCoupon - kupon kaldırılır")
    void removeCoupon_ShouldReturnCart() {
        when(cartService.removeCoupon(USER_ID)).thenReturn(cartResponse);

        ResultData<CartResponse> result = controller.removeCoupon();

        assertTrue(result.isStatus());
        verify(cartService).removeCoupon(USER_ID);
    }
}
