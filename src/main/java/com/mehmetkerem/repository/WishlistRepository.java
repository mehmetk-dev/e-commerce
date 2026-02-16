package com.mehmetkerem.repository;

import com.mehmetkerem.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Long> {
    Optional<WishList> findByUserId(Long userId);
}
