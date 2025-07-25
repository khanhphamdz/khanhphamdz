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
import org.springframework.http.HttpStatus;
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
import com.datn.teeshirt.DTO.OrderResponseDTO;
<<<<<<< HEAD
import com.datn.teeshirt.DTO.OrderStatusDTO;
=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
import com.datn.teeshirt.DTO.ReturnRequestDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.CustomerAddress;
import com.datn.teeshirt.Security.CustomUserDetailsService;
import com.datn.teeshirt.Service.CustomerAddressService;
import com.datn.teeshirt.Service.CustomerService;
import com.datn.teeshirt.Service.OrderService;
import com.datn.teeshirt.Service.ReturnRequestService;
<<<<<<< HEAD
=======
import com.datn.teeshirt.Service.WishlistService;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    OrderService orderService;

    @Autowired
    private ReturnRequestService returnRequestService;

    @Autowired
    OrderService orderService;

    @Autowired
    private ReturnRequestService returnRequestService;

    @GetMapping
    public String accountPage(Model model, HttpServletRequest request) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            // Nếu chưa đăng nhập, redirect về trang login
            return "redirect:/account/login";
        }
        // Load customer addresses
        List<CustomerAddress> addresses = customerAddressService.findByCustomerId(currentCustomer.getCustomerId());
        currentCustomer.setAddresses(addresses);
        model.addAttribute("customer", currentCustomer);
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
    @PostMapping("/address/add")
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
<<<<<<< HEAD
    public ResponseEntity<?> updateAddressDTO(@PathVariable("id") Long addressId,
            @ModelAttribute CustomerAddressDTO dto) {
=======
    public ResponseEntity<?> updateAddressDTO(@PathVariable("id") Long addressId, @ModelAttribute CustomerAddressDTO dto) {
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        CustomerAddress address = customerAddressService.findById(addressId);
        if (address == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy địa chỉ", null));
        }
        // Cập nhật các trường từ DTO
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvinceId(dto.getProvinceId());
        address.setDistrictId(dto.getDistrictId());
        address.setWardId(dto.getWardId());
        address.setSpecificAddress(dto.getSpecificAddress());
        customerAddressService.save(address);
        return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật địa chỉ thành công", null));
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

    // API: Lấy tất cả đơn hàng
    @GetMapping("/orders")
    @ResponseBody
    public ResponseEntity<ResponseObject> getAllOrders() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách đơn hàng thành công", orders));
    }

    // API: Lấy đơn hàng theo trạng thái
    @GetMapping("/orders/{status}")
    @ResponseBody
    public ResponseEntity<ResponseObject> getOrdersByStatus(@PathVariable("status") String status) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerAndStatus(currentCustomer.getCustomerId(),
                status);
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy đơn hàng thành công", orders));
    }

    // API: Hủy đơn hàng
    @PostMapping("/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<ResponseObject> cancelOrder(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        boolean success = orderService.cancelOrder(orderId, currentCustomer.getCustomerId());
        if (success) {
            return ResponseEntity.ok(new ResponseObject("ok", "Hủy đơn hàng thành công", null));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không thể hủy đơn hàng này", null));
        }
    }

    // API: Lấy chi tiết đơn hàng
    @GetMapping("/orders/{orderId}/detail")
    @ResponseBody
    public ResponseEntity<ResponseObject> getOrderDetail(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        OrderResponseDTO orderDetail = orderService.getOrderDetail(orderId, currentCustomer.getCustomerId());
        if (orderDetail != null) {
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy chi tiết đơn hàng thành công", orderDetail));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy đơn hàng", null));
        }
    }

    // API: Tạo yêu cầu hoàn trả
    @PostMapping("/return-request")
    @ResponseBody
    public ResponseEntity<ResponseObject> createReturnRequest(
            @RequestParam("orderId") Long orderId,
            @RequestParam("reason") String reason,
            @RequestParam("description") String description,
            @RequestParam("products") String productsJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam("returnType") String returnType) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }
        boolean success = returnRequestService.createReturnRequest(orderId, reason, description, productsJson, images,
                currentCustomer.getCustomerId(), returnType);
        if (success) {
            return ResponseEntity.ok(new ResponseObject("ok", "Tạo yêu cầu hoàn trả thành công", null));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không thể tạo yêu cầu hoàn trả", null));
        }
    }

    // API: Lấy danh sách yêu cầu hoàn trả của khách hàng
    @GetMapping("/return-requests")
    @ResponseBody
    public ResponseEntity<ResponseObject> getReturnRequests() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }
        // Lấy danh sách return request (dùng DTO)
        List<ReturnRequestDTO> returnRequests = returnRequestService
                .getReturnRequestDTOsByCustomer(currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách yêu cầu hoàn trả thành công", returnRequests));
    }

    // API: Lấy return request theo orderId
    @GetMapping("/return-request/{orderId}")
    @ResponseBody
    public ResponseEntity<ResponseObject> getReturnRequestByOrderId(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        ReturnRequestDTO returnRequest = returnRequestService.getReturnRequestByOrderId(orderId,
                currentCustomer.getCustomerId());
        if (returnRequest != null) {
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy yêu cầu hoàn trả thành công", returnRequest));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy yêu cầu hoàn trả", null));
        }
    }

    // API: Lấy lịch sử trạng thái đơn hàng
    @GetMapping("/orders/{orderId}/status-history")
    @ResponseBody
    public ResponseEntity<ResponseObject> getOrderStatusHistory(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }
        List<OrderStatusDTO> statusHistory = orderService.getOrderStatusHistoryFullDTO(orderId,
                currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy lịch sử trạng thái đơn hàng thành công", statusHistory));
    }

    // API: Hủy return request
    @PostMapping("/return-request/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<ResponseObject> cancelReturnRequest(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        boolean success = returnRequestService.cancelReturnRequest(orderId, currentCustomer.getCustomerId());
        if (success) {
            return ResponseEntity.ok(new ResponseObject("ok", "Hủy yêu cầu hoàn trả thành công", null));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không thể hủy yêu cầu hoàn trả", null));
        }
    }

    /**
     * API cập nhật thông tin tài khoản
     * Mọi việc validate dữ liệu được thực hiện ở phía client
     */
    @PostMapping("/update-profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy dữ liệu từ request
            String name = requestBody.get("name");
            String phone = requestBody.get("phone");

            // Lấy thông tin khách hàng đang đăng nhập
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy thông tin khách hàng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Cập nhật thông tin
            if (name != null && !name.isEmpty()) {
                currentCustomer.setName(name);
            }

            if (phone != null) {
                currentCustomer.setPhone(phone);
            }

            // Lưu vào database (sử dụng customerService cho nhất quán)
            customerService.save(currentCustomer);

            response.put("status", "ok");
            response.put("message", "Cập nhật thông tin thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/address/set-default/{id}")
    @ResponseBody
    public ResponseEntity<?> setDefaultAddress(@PathVariable("id") Long addressId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin khách hàng");
        }
        customerAddressService.setDefaultAddress(currentCustomer.getCustomerId(), addressId);
        return ResponseEntity.ok("Cập nhật địa chỉ mặc định thành công");
    }
<<<<<<< HEAD
=======
    
    // API: Lấy tất cả đơn hàng
    @GetMapping("/orders")
    @ResponseBody
    public ResponseEntity<ResponseObject> getAllOrders() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách đơn hàng thành công", orders));
    }

    // API: Lấy đơn hàng theo trạng thái
    @GetMapping("/orders/{status}")
    @ResponseBody
    public ResponseEntity<ResponseObject> getOrdersByStatus(@PathVariable("status") String status) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerAndStatus(currentCustomer.getCustomerId(), status);
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy đơn hàng thành công", orders));
    }

    // API: Hủy đơn hàng
    @PostMapping("/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<ResponseObject> cancelOrder(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        boolean success = orderService.cancelOrder(orderId, currentCustomer.getCustomerId());
        if (success) {
            return ResponseEntity.ok(new ResponseObject("ok", "Hủy đơn hàng thành công", null));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không thể hủy đơn hàng này", null));
        }
    }

    // API: Lấy chi tiết đơn hàng
    @GetMapping("/orders/{orderId}/detail")
    @ResponseBody
    public ResponseEntity<ResponseObject> getOrderDetail(@PathVariable("orderId") Long orderId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }

        OrderResponseDTO orderDetail = orderService.getOrderDetail(orderId, currentCustomer.getCustomerId());
        if (orderDetail != null) {
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy chi tiết đơn hàng thành công", orderDetail));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy đơn hàng", null));
        }
    }

    // API: Tạo yêu cầu hoàn trả
    @PostMapping("/return-request")
    @ResponseBody
    public ResponseEntity<ResponseObject> createReturnRequest(
            @RequestParam("orderId") Long orderId,
            @RequestParam("reason") String reason,
            @RequestParam("description") String description,
            @RequestParam("returnType") String returnType,
            @RequestParam("products") String productsJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }
        // Parse productsJson thành List<ReturnProductDTO> (tùy bạn định nghĩa DTO)
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(orderId);
        dto.setReturnReason(reason);
        dto.setReturnNote(description);
        dto.setReturnType(returnType);
        // Xử lý ảnh nếu có
        if (images != null && !images.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (MultipartFile file : images) {
                String fileName = file.getOriginalFilename();
                sb.append("/images/return/").append(fileName).append(",");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            dto.setReturnImages(sb.toString());
        }
        // Parse productsJson thành List<ReturnRequestItemDTO>
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.util.List<ReturnRequestDTO.ReturnRequestItemDTO> items = mapper.readValue(productsJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ReturnRequestDTO.ReturnRequestItemDTO>>(){});
            dto.setReturnItems(items);
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Lỗi parse sản phẩm hoàn trả", null));
        }
        boolean success = returnRequestService.createReturnRequest(dto, currentCustomer.getCustomerId());
        if (success) {
            return ResponseEntity.ok(new ResponseObject("ok", "Tạo yêu cầu hoàn trả thành công", null));
        } else {
            return ResponseEntity.ok(new ResponseObject("false", "Không thể tạo yêu cầu hoàn trả", null));
        }
    }

    // API: Lấy danh sách yêu cầu hoàn trả của khách hàng
    @GetMapping("/return-requests")
    @ResponseBody
    public ResponseEntity<ResponseObject> getReturnRequests() {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.ok(new ResponseObject("false", "Không tìm thấy thông tin khách hàng", null));
        }
        // Lấy danh sách return request (dùng DTO)
        List<ReturnRequestDTO> returnRequests = returnRequestService.getReturnRequestDTOsByCustomer(currentCustomer.getCustomerId());
        return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách yêu cầu hoàn trả thành công", returnRequests));
    }

    /**
     * API cập nhật thông tin tài khoản
     * Mọi việc validate dữ liệu được thực hiện ở phía client
     */
    @PostMapping("/update-profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy dữ liệu từ request
            String name = requestBody.get("name");
            String phone = requestBody.get("phone");

            // Lấy thông tin khách hàng đang đăng nhập
            Customer currentCustomer = customUserDetailsService.getCustomerInfo();
            if (currentCustomer == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy thông tin khách hàng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Cập nhật thông tin
            if (name != null && !name.isEmpty()) {
                currentCustomer.setName(name);
            }

            if (phone != null) {
                currentCustomer.setPhone(phone);
            }

            // Lưu vào database (sử dụng customerService cho nhất quán)
            customerService.save(currentCustomer);

            response.put("status", "ok");
            response.put("message", "Cập nhật thông tin thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/address/set-default/{id}")
    @ResponseBody
    public ResponseEntity<?> setDefaultAddress(@PathVariable("id") Long addressId) {
        Customer currentCustomer = customUserDetailsService.getCustomerInfo();
        if (currentCustomer == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin khách hàng");
        }
        customerAddressService.setDefaultAddress(currentCustomer.getCustomerId(), addressId);
        return ResponseEntity.ok("Cập nhật địa chỉ mặc định thành công");
    }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
}
