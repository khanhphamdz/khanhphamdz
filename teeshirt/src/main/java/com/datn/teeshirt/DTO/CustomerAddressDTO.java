package com.datn.teeshirt.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressDTO {
    private Long addressId;
    private Long customerId;
    private String provinceId;
    private String districtId;
    private String wardId;
    private String specificAddress;
    private String phone;
    private String name;
    private LocalDateTime createdAt;
} 