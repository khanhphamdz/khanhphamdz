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
    private String type; // 'percentage' (giảm theo %) hoặc 'fixed' (giảm số tiền cố định). Nếu giảm 50% thì type phải là 'percentage', nếu giảm 50.000đ thì type là 'fixed'.
} 
