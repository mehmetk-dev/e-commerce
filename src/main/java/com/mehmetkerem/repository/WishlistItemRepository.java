package com.mehmetkerem.repository;

import com.mehmetkerem.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishListIdAndProductId(Long wishListId, Long productId);

    void deleteByWishListIdAndProductId(Long wishListId, Long productId);
}
