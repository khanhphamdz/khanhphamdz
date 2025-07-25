package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.ReturnRequestItem;

@Repository
public interface ReturnRequestItemRepository extends JpaRepository<ReturnRequestItem, Long> {

    @Query("SELECT ri FROM ReturnRequestItem ri WHERE ri.returnRequest.returnId = :returnId")
    List<ReturnRequestItem> findByReturnRequestId(@Param("returnId") Long returnId);
}
