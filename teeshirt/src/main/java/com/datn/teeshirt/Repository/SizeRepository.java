package com.datn.teeshirt.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    Page<Size> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
} 