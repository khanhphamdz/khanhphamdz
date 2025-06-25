package com.datn.teeshirt.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Long imageId;
    private Long productId;
    private Long variantId;
    private String imageUrl;
    private String imageType;
    private java.time.LocalDateTime createdAt;
}