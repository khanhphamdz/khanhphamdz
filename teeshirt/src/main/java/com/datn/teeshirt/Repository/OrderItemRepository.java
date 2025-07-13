package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_OrderId(Long orderId);
    List<OrderItem> findByVariant_VariantId(Long variantId);
} 