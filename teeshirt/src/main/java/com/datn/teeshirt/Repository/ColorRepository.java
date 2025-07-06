package com.datn.teeshirt.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    Page<Color> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByHexCodeIgnoreCase(String hexCode);
} 