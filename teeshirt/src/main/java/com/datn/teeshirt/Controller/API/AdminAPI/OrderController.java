package com.datn.teeshirt.Controller.API.AdminAPI;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.OrderCreateRequestDTO;
import com.datn.teeshirt.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/counter-sale/orders")
public class OrderController {
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

    // Lấy danh sách đơn hàng có phân trang, tìm kiếm, lọc
    @GetMapping
    public ResponseEntity<ResponseObject> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate createdDate
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách đơn hàng thành công", orderService.searchOrders(pageable, keyword, status, orderType, createdDate)));
    }
} 