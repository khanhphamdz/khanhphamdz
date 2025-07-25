package com.datn.teeshirt.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PromotionDTO {
    private Long promotionId;
    private String name;
    private String description;
    private BigDecimal discountValue;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private List<Long> productIds;
    private List<Long> variantIds;
    private List<Long> categoryIds;
    private String applyType;
}
