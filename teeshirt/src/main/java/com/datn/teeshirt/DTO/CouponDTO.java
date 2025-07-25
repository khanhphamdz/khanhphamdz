package com.datn.teeshirt.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CouponDTO {
    private Long couponId;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer usageCount;
    private Boolean isActive;
    private String applyToCustomer;
    private String type; // 'percentage' (gi?m theo %) ho?c 'fixed' (gi?m s? ti?n c? d?nh). N?u gi?m 50% thì type ph?i là 'percentage', n?u gi?m 50.000d thì type là 'fixed'.
} 
