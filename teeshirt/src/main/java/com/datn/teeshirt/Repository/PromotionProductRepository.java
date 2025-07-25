package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.teeshirt.Entity.PromotionProduct;
import com.datn.teeshirt.Entity.PromotionProduct.PromotionProductId;

import jakarta.transaction.Transactional;
import java.util.List;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, PromotionProductId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PromotionProduct pp WHERE pp.promotion.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Long promotionId);

    List<PromotionProduct> findByVariant_VariantId(Long variantId);
} 
