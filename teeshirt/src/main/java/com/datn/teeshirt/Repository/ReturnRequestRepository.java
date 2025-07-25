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

<<<<<<< HEAD
    // Tìm ki?m và phân trang theo tr?ng thái, t? khóa (orderId, customer name, phone)
=======
    // TÃ¬m kiáº¿m vÃ  phÃ¢n trang theo tráº¡ng thÃ¡i, tá»« khÃ³a (orderId, customer name, phone)
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    @Query("SELECT r FROM ReturnRequest r WHERE (:status IS NULL OR r.returnStatus = :status) " +
           "AND (:keyword IS NULL OR CAST(r.order.orderId AS string) LIKE %:keyword% " +
           "OR LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR r.customer.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<ReturnRequest> searchByStatusAndKeyword(@Param("status") ReturnRequest.ReturnStatus status,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}
