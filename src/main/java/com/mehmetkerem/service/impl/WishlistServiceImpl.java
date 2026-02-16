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
import com.mehmetkerem.service.IWishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
@Slf4j
public class WishlistServiceImpl implements IWishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final IUserService userService;
    private final IProductService productService;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse getWishlistByUserId(Long userId) {
        WishList wishlist = getOrCreateWishlist(userId);
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse addItemToWishlist(Long userId, Long productId) {
        WishList wishlist = getOrCreateWishlist(userId);
        Product product = productService.getProductById(productId);

        Optional<WishlistItem> existingItem = wishlistItemRepository.findByWishListIdAndProductId(wishlist.getId(),
                productId);

        if (existingItem.isPresent()) {
            throw new BadRequestException("Ürün zaten favori listenizde ekli!");
        }

        WishlistItem newItem = new WishlistItem();
        newItem.setWishList(wishlist);
        newItem.setProduct(product);
        newItem.setAddedTime(LocalDateTime.now());

        wishlist.getItems().add(newItem);
        wishlistRepository.save(wishlist);

        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public void removeItemFromWishlist(Long userId, Long productId) {
        WishList wishlist = getOrCreateWishlist(userId);

        WishlistItem itemToRemove = wishlistItemRepository.findByWishListIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Ürün favori listenizde bulunamadı!"));

        wishlist.getItems().remove(itemToRemove);
        wishlistItemRepository.delete(itemToRemove);
    }

    @Override
    @Transactional
    public void clearWishlist(Long userId) {
        WishList wishlist = getOrCreateWishlist(userId);
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
    }

    private WishList getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId);
                    WishList newWishlist = new WishList();
                    newWishlist.setUser(user);
                    newWishlist.setItems(new ArrayList<>());
                    return wishlistRepository.save(newWishlist);
                });
    }
}