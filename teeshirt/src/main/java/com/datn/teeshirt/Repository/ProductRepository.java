package com.datn.teeshirt.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.isFeatured = true")
    Page<Product> findAllActive(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword% AND p.isFeatured = true")
    Page<Product> search(String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isFeatured = true ORDER BY p.createdAt DESC")
    Page<Product> findLatestProducts(Pageable pageable);
} 