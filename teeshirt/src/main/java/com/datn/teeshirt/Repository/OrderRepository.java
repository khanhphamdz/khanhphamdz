package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

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

    List<Order> findByCustomerCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, String status);

    List<Order> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.customer.customerId = :customerId")
    Optional<Order> findByOrderIdAndCustomerId(@Param("orderId") Long orderId, @Param("customerId") Long customerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :customerId AND o.coupon.couponId = :couponId")
    long countByCustomerIdAndCouponId(@Param("customerId") Long customerId, @Param("couponId") Long couponId);
<<<<<<< HEAD

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderStatuses WHERE o.orderId = :orderId AND o.customer.customerId = :customerId")
    Optional<Order> findByOrderIdAndCustomerIdWithStatuses(@Param("orderId") Long orderId, @Param("customerId") Long customerId);

    @Query("""
        SELECT o FROM Order o
        
        ORDER BY o.createdAt DESC
    """)
    Page<Order> searchOrders(
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("orderType") String orderType,
        @Param("createdDate") java.time.LocalDate createdDate,
        Pageable pageable
    );
=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
}
