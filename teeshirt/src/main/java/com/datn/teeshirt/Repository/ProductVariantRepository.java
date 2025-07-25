package com.datn.teeshirt.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Color;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Entity.Size;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :productId AND pv.isActive = true AND pv.deletedAt IS NULL")
    List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.quantityInStock > 0 AND pv.isActive = true AND pv.deletedAt IS NULL")
    List<ProductVariant> findInStock();

    List<ProductVariant> findByProduct_ProductIdAndDeletedAtIsNull(Long productId);

    List<ProductVariant> findByProduct_ProductId(Long productId);

    // Lấy biến thể theo màu sắc
    List<ProductVariant> findByProduct_ProductIdAndColor_ColorIdAndDeletedAtIsNull(Long productId, Long colorId);

    // Lấy biến thể theo size
    List<ProductVariant> findByProduct_ProductIdAndSize_SizeIdAndDeletedAtIsNull(Long productId, Long sizeId);

    // Kiểm tra biến thể đã tồn tại
    boolean existsByProductAndColorAndSizeAndDeletedAtIsNull(Product product, Color color, Size size);

    boolean existsByProduct_ProductIdAndColor_ColorIdAndSize_SizeIdAndDeletedAtIsNull(Long productId, Long colorId, Long sizeId);

    boolean existsByBarcodeAndDeletedAtIsNull(String barcode);

    Optional<ProductVariant> findByBarcodeAndDeletedAtIsNull(String barcode);
}