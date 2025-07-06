package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.teeshirt.Entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.variant.variantId = :variantId")
    void deleteByCartIdAndVariantId(@Param("cartId") Long cartId, @Param("variantId") Long variantId);
} 