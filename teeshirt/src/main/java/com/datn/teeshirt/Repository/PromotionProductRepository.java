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

<<<<<<< HEAD
    @Modifying
    @Transactional
    @Query("DELETE FROM PromotionProduct pp WHERE pp.variant.variantId = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);

=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    List<PromotionProduct> findByVariant_VariantId(Long variantId);
} 
