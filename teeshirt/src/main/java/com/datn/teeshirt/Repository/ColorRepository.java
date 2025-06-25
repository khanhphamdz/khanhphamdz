package com.datn.teeshirt.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
} 