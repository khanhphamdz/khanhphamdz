package com.datn.teeshirt.DTO;

import java.util.List;

import lombok.Data;

@Data
public class CartDTO {
    private Long customerId;
    private String customerName;
    private List<CartItemDTO> cartItems;
}
