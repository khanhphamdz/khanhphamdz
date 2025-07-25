package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.teeshirt.Entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Override
    @EntityGraph(attributePaths = "promotionProducts")
    List<Promotion> findAll();

    @EntityGraph(attributePaths = "promotionProducts")
    List<Promotion> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = "promotionProducts")
<<<<<<< HEAD
    org.springframework.data.domain.Page<Promotion> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);} 
=======
    org.springframework.data.domain.Page<Promotion> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
} 
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
