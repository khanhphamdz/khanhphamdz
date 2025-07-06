package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByProduct_ProductId(Long productId);
    List<ProductCategory> findByCategory_CategoryId(Long categoryId);
    void deleteByProduct_ProductId(Long productId);
} 