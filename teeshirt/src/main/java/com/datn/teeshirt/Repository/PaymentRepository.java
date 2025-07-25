package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_OrderId(Long orderId);
    List<Payment> findByTransactionId(String transactionId);
} 