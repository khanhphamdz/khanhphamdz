package com.datn.teeshirt.Repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Lấy tất cả sản phẩm đang hoạt động (status = true)
    @Query("SELECT p FROM Product p WHERE p.status = true")
    Page<Product> findAllActive(Pageable pageable);

    // Tìm kiếm sản phẩm theo tên, mô tả, danh mục
    @Query("SELECT p FROM Product p WHERE p.status = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    // Lọc sản phẩm theo danh mục, giá, trạng thái
    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.categoryId = :categoryId) AND (:status IS NULL OR p.status = :status) AND (:minPrice IS NULL OR p.basePrice >= :minPrice) AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) AND p.deletedAt IS NULL")
    Page<Product> filter(@Param("categoryId") Long categoryId, @Param("status") Boolean status, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    // Lấy sản phẩm mới nhất
    @Query("SELECT p FROM Product p WHERE p.status = true AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);

    // Lấy sản phẩm giảm giá (có discountPrice khác null và nhỏ hơn price)
    @Query("SELECT p FROM Product p JOIN p.variants v WHERE v.discountPrice IS NOT NULL AND v.discountPrice < v.price AND p.status = true AND p.deletedAt IS NULL")
    List<Product> findDiscountedProducts(Pageable pageable);
}