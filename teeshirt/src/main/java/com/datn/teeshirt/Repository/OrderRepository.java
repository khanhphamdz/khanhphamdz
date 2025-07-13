package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer_CustomerId(Long customerId);
    List<Order> findByEmployee_EmployeeId(Long employeeId);
    List<Order> findByStatus(String status);
    List<Order> findByOrderType(String orderType);
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Order> findByStatusAndCreatedAtBetween(String status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = :status")
    Double sumFinalAmountByDateRangeAndStatus(LocalDateTime from, LocalDateTime to, String status);

    long countByStatus(String status);

    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('DATE', o.createdAt) = FUNCTION('DATE', :date)")
    long countByDate(LocalDateTime date);
}
