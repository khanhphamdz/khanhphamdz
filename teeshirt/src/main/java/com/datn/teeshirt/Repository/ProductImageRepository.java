package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_ProductId(Long productId);
    List<ProductImage> findByVariant_VariantId(Long variantId);
    List<ProductImage> findByProduct_ProductIdAndImageType(Long productId, String imageType);
} 