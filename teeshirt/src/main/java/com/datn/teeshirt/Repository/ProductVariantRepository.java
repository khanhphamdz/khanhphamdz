package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isActive = true")
    List<ProductVariant> findActiveByProductId(Long productId);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.quantityInStock > 0 AND pv.isActive = true")
    List<ProductVariant> findInStock();
} 