package com.datn.teeshirt.DTO;

import java.time.LocalDateTime;

public class OrderStatusDTO {
    private String statusName;
    private LocalDateTime createdAt;
    // Getter & Setter
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
