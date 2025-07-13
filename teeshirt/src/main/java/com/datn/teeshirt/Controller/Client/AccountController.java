package com.datn.teeshirt.Controller.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.CustomerAddressDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.CustomerAddress;
import com.datn.teeshirt.Entity.WishlistItem;
import com.datn.teeshirt.Security.CustomUserDetailsService;
import com.datn.teeshirt.Service.CustomerAddressService;
import com.datn.teeshirt.Service.CustomerService;
import com.datn.teeshirt.Service.WishlistService;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerAddressService customerAddressService;

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public String accountPage(Model model) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer != null) {
            // Load customer addresses
            List<CustomerAddress> addresses = customerAddressService.findByCustomerId(currentCustomer.getCustomerId());
            currentCustomer.setAddresses(addresses);

            model.addAttribute("customer", currentCustomer);
        }
        return "customer/account/account";
    }

    @PostMapping("/upload-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                response.put("error", "Không tìm thấy thông tin khách hàng");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.isEmpty()) {
                response.put("error", "Vui lòng chọn file ảnh");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // Đường dẫn lưu file
            Path uploadPath = Paths.get("src/main/resources/static/images/avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật database
            String avatarUrl = "/images/avatars/" + fileName;
            currentCustomer.setAvatarUrl(avatarUrl);
            customerService.save(currentCustomer);

            response.put("success", "Upload thành công");
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Lỗi upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API: Lấy thông tin khách hàng hiện tại (DTO)
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<?> getCurrentCustomerInfo() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin khách hàng");
        }
        CustomerDTO dto = customerService.toDTO(currentCustomer);
        return ResponseEntity.ok(dto);
    }

    // API: Lấy danh sách địa chỉ của khách hàng hiện tại (DTO)
    @GetMapping("/address/list")
    @ResponseBody
    public ResponseEntity<ResponseObject> getCurrentCustomerAddresses() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy khách hàng", null));
        }
        List<CustomerAddressDTO> dtos = customerAddressService
                .getAddressDTOsByCustomerId(currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Tìm thấy thông tin khách hàng", dtos));
    }

    // API: Lấy danh sách địa chỉ của customer theo customerId (DTO)
    @GetMapping("/address/list/{customerId}")
    @ResponseBody
    public ResponseEntity<?> getCustomerAddressesById(@PathVariable("customerId") Long customerId) {
        List<CustomerAddressDTO> dtos = customerAddressService.getAddressDTOsByCustomerId(customerId);
        return ResponseEntity.ok(dtos);
    }

    // API: Thêm địa chỉ mới (dùng DTO)
    @PostMapping("/address/add-dto")
    @ResponseBody
    public ResponseEntity<ResponseObject> addAddressDTO(@ModelAttribute CustomerAddressDTO dto) {
        try {
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            CustomerAddress address = customerAddressService.toEntity(dto, currentCustomer);
            customerAddressService.save(address);
            return ResponseEntity.ok(new ResponseObject("ok", "Thêm địa chỉ thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Lỗi", e.getMessage()));
        } 
        
    }

    // API: Sửa địa chỉ (dùng DTO)
    @PutMapping("/address/update-dto/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAddressDTO(@PathVariable("id") Long addressId, @RequestBody CustomerAddressDTO dto) {
        CustomerAddress address = customerAddressService.findById(addressId);
        if (address == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy địa chỉ");
        }
        // Cập nhật các trường từ DTO
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvinceId(dto.getProvinceId());
        address.setDistrictId(dto.getDistrictId());
        address.setWardId(dto.getWardId());
        address.setSpecificAddress(dto.getSpecificAddress());
        customerAddressService.save(address);
        return ResponseEntity.ok("Cập nhật địa chỉ thành công");
    }

    // API: Xóa địa chỉ (giữ nguyên)
    @DeleteMapping("/address/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable("id") Long addressId) {
        Map<String, Object> response = new HashMap<>();

        try {
            customerAddressService.deleteAddress(addressId);
            response.put("success", true);
            response.put("message", "Xóa địa chỉ thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi xóa địa chỉ: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "customer/account/login-register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "customer/account/forgot-password";
    }
    @GetMapping("/wishlist")
    public String WishlistPage(Model model) {
        Customer customer = customUserDetailsService.getCustomerInfo();
        List<WishlistItem> wishlistItems = wishlistService.getWishlistItems(customer);
        model.addAttribute("wishlistItems", wishlistItems != null ? wishlistItems : List.of());
        return "customer/account/wishlist";
    }
    
}
