package com.datn.teeshirt.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.datn.teeshirt.Entity.CustomerAddress;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long customerId;
    private String name;
    private String password;
    private String email;
    private String avatarUrl;
    List<CustomerAddress> listCustomerAddresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}