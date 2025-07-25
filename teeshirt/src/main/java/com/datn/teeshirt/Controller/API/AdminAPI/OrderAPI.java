package com.datn.teeshirt.Controller.API.AdminAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.OrderResponseDTO;
import com.datn.teeshirt.Service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/admin/orders")
public class OrderAPI {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(name="keyword", required = false) String keyword) {
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách đơn hàng thành công", orderService.getAllOrders()));
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<ResponseObject> getOrderById(@PathVariable Long orderId) {
        try {
            OrderResponseDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy đơn hàng thành công", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject("error", "Lấy đơn hàng thất bại", e.getMessage()));
        }
    }
}
