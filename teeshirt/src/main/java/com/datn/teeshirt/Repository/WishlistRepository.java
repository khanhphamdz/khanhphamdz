package com.datn.teeshirt.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Wishlist;
import com.datn.teeshirt.Entity.WishlistItem;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByCustomerCustomerId(Long customerId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.customer.customerId = :customerId")
    Optional<Wishlist> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.customer.customerId = :customerId")
    List<WishlistItem> findWishlistItemsByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.customer.customerId = :customerId AND wi.product.productId = :productId")
    Optional<WishlistItem> findByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);
    
    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.customer.customerId = :customerId")
    int countWishlistItemsByCustomerId(@Param("customerId") Long customerId);
}
