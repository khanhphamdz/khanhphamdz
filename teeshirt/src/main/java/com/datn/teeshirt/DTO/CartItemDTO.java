package com.datn.teeshirt.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDTO {
    private Long cartItemId;
    private Long variantId;
    private String productName;
    private String color;
    private String size;
    private Integer quantity;
    private Double price;
    private String imageUrl;
} 