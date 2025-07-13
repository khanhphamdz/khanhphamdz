package com.datn.teeshirt.DTO;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long productVariantId;
    private int quantity;
    private BigDecimal price;

    // Getter & Setter
    public Long getProductVariantId() { return productVariantId; }
    public void setProductVariantId(Long productVariantId) { this.productVariantId = productVariantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
} 