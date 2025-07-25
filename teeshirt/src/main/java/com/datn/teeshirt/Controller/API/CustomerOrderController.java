package com.datn.teeshirt.Controller.API;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.OrderCreateRequestDTO;
import com.datn.teeshirt.DTO.OrderResponseDTO;
import com.datn.teeshirt.Service.OrderService;
import com.datn.teeshirt.Security.CustomUserDetailsService;
import com.datn.teeshirt.Entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Tạo đơn hàng mới cho customer
    @PostMapping
    public ResponseEntity<ResponseObject> createOrder(@RequestBody OrderCreateRequestDTO orderDTO, Authentication authentication) {
        try {
            // Lấy thông tin customer từ authentication
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
            }
            
            // Set customer ID vào order
            orderDTO.setCustomerId(currentCustomer.getCustomerId());
            orderDTO.setOrderType("online"); // Customer đặt hàng online
            
            // Tạo đơn hàng
            OrderResponseDTO createdOrder = orderService.createOrder(orderDTO);
            
            return ResponseEntity.ok(new ResponseObject("ok", "Đặt hàng thành công!", createdOrder));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new ResponseObject("false", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra khi đặt hàng: " + e.getMessage(), null));
        }
    }

    // Lấy danh sách đơn hàng của customer
    @GetMapping
    public ResponseEntity<ResponseObject> getCustomerOrders(Authentication authentication) {
        try {
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
            }
            
            var orders = orderService.getOrdersByCustomer(currentCustomer.getCustomerId());
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách đơn hàng thành công", orders));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }

    // Lấy chi tiết đơn hàng của customer
    @GetMapping("/{orderId}")
    public ResponseEntity<ResponseObject> getOrderDetail(@PathVariable Long orderId, Authentication authentication) {
        try {
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
            }
            
            OrderResponseDTO order = orderService.getOrderDetail(orderId, currentCustomer.getCustomerId());
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy chi tiết đơn hàng thành công", order));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new ResponseObject("false", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }

    // Hủy đơn hàng
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseObject> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        try {
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
            }
            
            boolean success = orderService.cancelOrder(orderId, currentCustomer.getCustomerId());
            if (success) {
                return ResponseEntity.ok(new ResponseObject("ok", "Hủy đơn hàng thành công", null));
            } else {
                return ResponseEntity.ok(new ResponseObject("false", "Không thể hủy đơn hàng", null));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }
} 