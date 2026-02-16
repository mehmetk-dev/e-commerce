package com.mehmetkerem.service.impl;

import com.mehmetkerem.dto.response.WishlistResponse;
import com.mehmetkerem.exception.BadRequestException;
import com.mehmetkerem.exception.NotFoundException;
import com.mehmetkerem.mapper.WishlistMapper;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.model.User;
import com.mehmetkerem.model.WishList;
import com.mehmetkerem.model.WishlistItem;
import com.mehmetkerem.repository.WishlistItemRepository;
import com.mehmetkerem.repository.WishlistRepository;
import com.mehmetkerem.service.IProductService;
import com.mehmetkerem.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private IUserService userService;

    @Mock
    private IProductService productService;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private User user;
    private WishList wishlist;
    private Product product;
    private WishlistResponse wishlistResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).email("u@test.com").build();
        wishlist = new WishList();
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setItems(new ArrayList<>());
        product = Product.builder().id(PRODUCT_ID).title("Ürün").build();
        wishlistResponse = new WishlistResponse();
        wishlistResponse.setId(1L);
    }

    @Test
    @DisplayName("getWishlistByUserId - mevcut liste döner")
    void getWishlistByUserId_WhenExists_ShouldReturnWishlist() {
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponse(wishlist)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getWishlistByUserId - liste yoksa yeni oluşturulur")
    void getWishlistByUserId_WhenNotExists_ShouldCreateAndReturn() {
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userService.getUserById(USER_ID)).thenReturn(user);
        when(wishlistRepository.save(any(WishList.class))).thenAnswer(inv -> {
            WishList w = inv.getArgument(0);
            w.setId(2L);
            return w;
        });
        when(wishlistMapper.toResponse(any(WishList.class))).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.getWishlistByUserId(USER_ID);

        assertNotNull(result);
        verify(wishlistRepository).save(any(WishList.class));
        verify(userService).getUserById(USER_ID);
    }

    @Test
    @DisplayName("addItemToWishlist - ürün eklenir")
    void addItemToWishlist_WhenValid_ShouldAddItem() {
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);
        when(wishlistItemRepository.findByWishListIdAndProductId(1L, PRODUCT_ID)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(WishList.class))).thenReturn(wishlist);
        when(wishlistMapper.toResponse(wishlist)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.addItemToWishlist(USER_ID, PRODUCT_ID);

        assertNotNull(result);
        assertEquals(1, wishlist.getItems().size());
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    @DisplayName("addItemToWishlist - ürün zaten listede ise BadRequestException")
    void addItemToWishlist_WhenAlreadyInWishlist_ShouldThrowBadRequestException() {
        WishlistItem existingItem = new WishlistItem();
        existingItem.setProduct(product);
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);
        when(wishlistItemRepository.findByWishListIdAndProductId(1L, PRODUCT_ID))
                .thenReturn(Optional.of(existingItem));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> wishlistService.addItemToWishlist(USER_ID, PRODUCT_ID));

        assertTrue(ex.getMessage().contains("zaten"));
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeItemFromWishlist - ürün listeden çıkarılır")
    void removeItemFromWishlist_WhenItemExists_ShouldRemove() {
        WishlistItem item = new WishlistItem();
        item.setProduct(product);
        wishlist.getItems().add(item);
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishListIdAndProductId(1L, PRODUCT_ID)).thenReturn(Optional.of(item));
        doNothing().when(wishlistItemRepository).delete(item);

        wishlistService.removeItemFromWishlist(USER_ID, PRODUCT_ID);

        verify(wishlistItemRepository).delete(item);
        assertTrue(wishlist.getItems().isEmpty());
    }

    @Test
    @DisplayName("removeItemFromWishlist - ürün listede yoksa NotFoundException")
    void removeItemFromWishlist_WhenItemNotInWishlist_ShouldThrowNotFoundException() {
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishListIdAndProductId(1L, PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> wishlistService.removeItemFromWishlist(USER_ID, PRODUCT_ID));
        verify(wishlistItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("clearWishlist - liste temizlenir")
    void clearWishlist_ShouldClearAllItems() {
        wishlist.getItems().add(new WishlistItem());
        when(wishlistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(WishList.class))).thenReturn(wishlist);

        wishlistService.clearWishlist(USER_ID);

        assertTrue(wishlist.getItems().isEmpty());
        verify(wishlistRepository).save(wishlist);
    }
}
