package com.mehmetkerem.controller;

import com.mehmetkerem.controller.impl.RestWishlistControllerImpl;
import com.mehmetkerem.dto.response.WishlistResponse;
import com.mehmetkerem.service.IWishlistService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestWishlistControllerTest {

    @Mock
    private IWishlistService wishlistService;

    @InjectMocks
    private RestWishlistControllerImpl controller;

    private static final Long USER_ID = SecurityTestUtils.DEFAULT_USER_ID;
    private static final Long PRODUCT_ID = 10L;
    private WishlistResponse wishlistResponse;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.setCurrentUser();
        wishlistResponse = new WishlistResponse();
        wishlistResponse.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearContext();
    }

    @Test
    @DisplayName("getWishlist - liste döner")
    void getWishlist_ShouldReturnWishlist() {
        when(wishlistService.getWishlistByUserId(USER_ID)).thenReturn(wishlistResponse);

        ResultData<WishlistResponse> result = controller.getWishlist();

        assertTrue(result.isStatus());
        assertEquals(1L, result.getData().getId());
        verify(wishlistService).getWishlistByUserId(USER_ID);
    }

    @Test
    @DisplayName("addItem - ürün eklenir")
    void addItem_ShouldReturnWishlist() {
        when(wishlistService.addItemToWishlist(USER_ID, PRODUCT_ID)).thenReturn(wishlistResponse);

        ResultData<WishlistResponse> result = controller.addItem(PRODUCT_ID);

        assertTrue(result.isStatus());
        verify(wishlistService).addItemToWishlist(USER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("removeItem - mesaj döner")
    void removeItem_ShouldReturnSuccessMessage() {
        ResultData<String> result = controller.removeItem(PRODUCT_ID);

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("kaldırıldı"));
        verify(wishlistService).removeItemFromWishlist(USER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("clearWishlist - mesaj döner")
    void clearWishlist_ShouldReturnSuccessMessage() {
        ResultData<String> result = controller.clearWishlist();

        assertTrue(result.isStatus());
        assertTrue(result.getData().contains("temizlendi"));
        verify(wishlistService).clearWishlist(USER_ID);
    }
}
