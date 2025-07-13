package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
    List<OrderStatus> findByOrder_OrderId(Long orderId);
} 