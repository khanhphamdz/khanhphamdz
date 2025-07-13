package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByWishlistWishlistId(Long wishlistId);
    void deleteByWishlistWishlistIdAndProductProductId(Long wishlistId, Long productId);
    boolean existsByWishlistWishlistIdAndProductProductId(Long wishlistId, Long productId);
} 
