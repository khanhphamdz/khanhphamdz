package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    ShippingAddress findByOrder_OrderId(Long orderId);
} 