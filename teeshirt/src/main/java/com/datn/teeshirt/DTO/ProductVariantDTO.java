package com.datn.teeshirt.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Long variantId;
    private Long productId;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private LocalDateTime discountPriceStartAt;
    private LocalDateTime discountPriceEndAt;
    private Integer quantityInStock;
    private Boolean isActive;
    private List<AttributeDTO> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}