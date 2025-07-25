package com.datn.teeshirt.DTO;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long productVariantId;
    private int quantity;
    private BigDecimal price;
    private Long orderItemId;
    private BigDecimal priceAtPurchase;
    private ProductVariantDTO variant;

    // Getter & Setter
    public Long getProductVariantId() { return productVariantId; }
    public void setProductVariantId(Long productVariantId) { this.productVariantId = productVariantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(BigDecimal priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }

    public ProductVariantDTO getVariant() { return variant; }
    public void setVariant(ProductVariantDTO variant) { this.variant = variant; }
} 