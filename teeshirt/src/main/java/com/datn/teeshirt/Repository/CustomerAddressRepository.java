package com.datn.teeshirt.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.datn.teeshirt.Entity.CustomerAddress;

import jakarta.transaction.Transactional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerCustomerId(Long customerId);

    @Modifying
    @Transactional
    void deleteByAddressId(Long addressId);
}
