package com.datn.teeshirt.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDTO {
    private Long productId;
    private String name;
    private String description;
    private String shortDescription;
    private Boolean isFeatured;
    private Boolean status;
    private List<ProductAttributeDTO> attributes;
    private List<CategoryDTO> categories;
    private List<ProductVariantDTO> variants;
    private List<ProductImageDTO> images;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}