package com.mehmetkerem.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "wishlist_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private WishList wishList;

    @ManyToOne
    private Product product;

    private LocalDateTime addedTime;

    @PrePersist
    protected void onCreate() {
        if (addedTime == null) {
            addedTime = LocalDateTime.now();
        }
    }
}
