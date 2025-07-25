package com.datn.teeshirt.Controller.Admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.CustomerAddressDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Service.CustomerAddressService;
import com.datn.teeshirt.Service.CustomerService;

@Controller
@RequestMapping("/admin/customer")
public class AdminCustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerAddressService customerAddressService;

    @GetMapping
    public String customerManagementPage() {
        return "admin/account/customer-management";
    }

    // API: Lấy danh sách khách hàng với phân trang và lọc
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<ResponseObject> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String province) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CustomerDTO> customers = customerService.findAll();
            
            // Lọc theo tên
            if (name != null && !name.trim().isEmpty()) {
                customers = customers.stream()
                    .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Lọc theo số điện thoại
            if (phone != null && !phone.trim().isEmpty()) {
                customers = customers.stream()
                    .filter(c -> c.getPhone() != null && c.getPhone().contains(phone))
                    .collect(Collectors.toList());
            }
            
            // Lọc theo tỉnh thành (tạm thời bỏ qua vì cần async call)
            // TODO: Implement lọc theo tỉnh thành sau
            
            // Chuyển sang DTO
            List<CustomerDTO> customerDTOs = customers;
            
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách khách hàng thành công", customerDTOs));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Lỗi: " + e.getMessage(), null));
        }
    }

    // API: Lấy chi tiết khách hàng
    @GetMapping("/api/{customerId}")
    @ResponseBody
    public ResponseEntity<ResponseObject> getCustomerDetail(@PathVariable Long customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy khách hàng", null));
            }
            // Lấy địa chỉ mặc định
            CustomerAddressDTO defaultAddress = customerAddressService.getDefaultAddressDTOByCustomerId(customerId);
            // Tạo response object
            Map<String, Object> response = Map.of(
                "customer", convertToCustomerDTO(customer),
                "address", defaultAddress
            );
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy thông tin khách hàng thành công", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Lỗi: " + e.getMessage(), null));
        }
    }

    // Helper method để chuyển Customer sang CustomerDTO với thông tin bổ sung
    private CustomerDTO convertToCustomerDTO(Customer customer) {
        return customerService.toDTO(customer);
    }
} 