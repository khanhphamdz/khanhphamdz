package com.datn.teeshirt.DTO;

import java.time.LocalDateTime;

public class OrderStatusDTO {
    private Integer orderStatusId;
    private String statusName;
    private LocalDateTime createdAt;

    public Integer getOrderStatusId() { return orderStatusId; }
    public void setOrderStatusId(Integer orderStatusId) { this.orderStatusId = orderStatusId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 
