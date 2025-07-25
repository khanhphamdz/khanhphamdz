package com.datn.teeshirt.Controller.API.AdminAPI;

import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Service.CustomerService;
import com.datn.teeshirt.Service.CustomerAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter-sale/customers")
public class CounterSaleCustomerController {
    @Autowired private CustomerService customerService;
    @Autowired private CustomerAddressService customerAddressService;

    // Tìm kiếm khách hàng
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam String query) {
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    // Tạo khách hàng mới
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.createCustomer(customerDTO));
    }

    // Lấy danh sách địa chỉ của khách hàng
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<?> getCustomerAddresses(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerAddressService.getAddressDTOsByCustomerId(customerId));
    }
} 