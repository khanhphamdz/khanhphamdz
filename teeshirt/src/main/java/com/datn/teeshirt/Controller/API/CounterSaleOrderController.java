package com.datn.teeshirt.Controller.API;

import com.datn.teeshirt.DTO.OrderCreateRequestDTO;
import com.datn.teeshirt.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter-sale/orders")
public class CounterSaleOrderController {
    @Autowired private OrderService orderService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateRequestDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    // Lấy chi tiết đơn hàng (để in hóa đơn)
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
} 