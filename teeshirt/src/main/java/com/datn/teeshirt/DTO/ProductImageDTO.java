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
public class ProductImageDTO {
    private Long imageId;
    private String productId;
    private String variantId;
    private String imageUrl;
    private String image_type;;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}