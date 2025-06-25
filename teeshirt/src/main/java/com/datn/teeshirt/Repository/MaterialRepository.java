package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    // Có thể bổ sung các hàm tìm kiếm nâng cao nếu cần
} 