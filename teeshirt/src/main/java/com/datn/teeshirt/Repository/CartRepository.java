package com.datn.teeshirt.Repository;

import com.datn.teeshirt.Entity.Cart;
import com.datn.teeshirt.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
} 