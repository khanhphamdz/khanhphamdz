package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.ReturnRequest;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    @Query("SELECT r FROM ReturnRequest r WHERE r.customer.customerId = :customerId ORDER BY r.requestDate DESC")
    List<ReturnRequest> findByCustomerIdOrderByRequestDateDesc(@Param("customerId") Long customerId);

    @Query("SELECT r FROM ReturnRequest r WHERE r.returnStatus = :status ORDER BY r.requestDate DESC")
    List<ReturnRequest> findByReturnStatusOrderByRequestDateDesc(@Param("status") ReturnRequest.ReturnStatus status);

    long countByReturnStatus(ReturnRequest.ReturnStatus status);

    // Tìm ki?m và phân trang theo tr?ng thái, t? khóa (orderId, customer name, phone)
    @Query("SELECT r FROM ReturnRequest r WHERE (:status IS NULL OR r.returnStatus = :status) " +
           "AND (:keyword IS NULL OR CAST(r.order.orderId AS string) LIKE %:keyword% " +
           "OR LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR r.customer.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<ReturnRequest> searchByStatusAndKeyword(@Param("status") ReturnRequest.ReturnStatus status,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}
