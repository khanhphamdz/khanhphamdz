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

public class OrderDTO {
    private Long orderId;
    private Long customerId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String note;
    private String paymentMethod;
    private List<OrderStatusDTO> status;
    private LocalDateTime createdAt;
} 