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
<<<<<<< HEAD
    private String type; // 'percentage' (gi?m theo %) ho?c 'fixed' (gi?m s? ti?n c? d?nh). N?u gi?m 50% th� type ph?i l� 'percentage', n?u gi?m 50.000d th� type l� 'fixed'.
=======
    private String type; // 'percentage' (giảm theo %) hoặc 'fixed' (giảm số tiền cố định). Nếu giảm 50% thì type phải là 'percentage', nếu giảm 50.000đ thì type là 'fixed'.
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
} 
