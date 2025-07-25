package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    
    java.util.Optional<WishlistItem> findByWishlistAndProduct(com.datn.teeshirt.Entity.Wishlist wishlist, com.datn.teeshirt.Entity.Product product);
    void deleteByWishlistWishlistIdAndProductProductId(Long wishlistId, Long productId);
    boolean existsByWishlistWishlistIdAndProductProductId(Long wishlistId, Long productId);
} 