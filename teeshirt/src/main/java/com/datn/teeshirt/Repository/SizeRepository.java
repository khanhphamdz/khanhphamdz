package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    // Có thể bổ sung các hàm tìm kiếm nâng cao nếu cần
} 