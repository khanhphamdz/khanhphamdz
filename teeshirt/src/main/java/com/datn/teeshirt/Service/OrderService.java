package com.datn.teeshirt.Service;

import com.datn.teeshirt.DTO.*;
import com.datn.teeshirt.Entity.*;
import com.datn.teeshirt.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderStatusRepository orderStatusRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ShippingAddressRepository shippingAddressRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private EmailService emailService;

    // 1. Tạo đơn hàng mới
    @Transactional
    public OrderResponseDTO createOrder(OrderCreateRequestDTO requestDTO) {
        // Validate dữ liệu đầu vào (tồn kho, giá, ...)
        if (requestDTO.getItems() == null || requestDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }
        // Kiểm tra tồn kho từng sản phẩm
        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemDTO.getProductVariantId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm: " + itemDTO.getProductVariantId()));
            if (variant.getQuantityInStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + variant.getName() + " không đủ tồn kho");
            }
        }
        // Mapping DTO -> Entity
        Order order = new Order();
        order.setCustomer(requestDTO.getCustomerId() != null ? customerRepository.findById(requestDTO.getCustomerId()).orElse(null) : null);
        order.setEmployee(requestDTO.getEmployeeId() != null ? employeeRepository.findById(requestDTO.getEmployeeId()).orElse(null) : null);
        order.setCoupon(requestDTO.getCouponId() != null ? couponRepository.findById(requestDTO.getCouponId()).orElse(null) : null);
        order.setTotalAmount(requestDTO.getFinalAmount()); // Nên tính lại phía backend
        order.setShippingFee(requestDTO.getShippingFee());
        order.setDiscountAmount(requestDTO.getDiscountAmount());
        order.setFinalAmount(requestDTO.getFinalAmount());
        order.setOrderType(requestDTO.getOrderType());
        order.setStatus("pending");
        order.setNote(requestDTO.getNote());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Nếu có coupon, tăng usageCount (chỉ nếu là COD/cash)
        boolean isCOD = false;
        if (requestDTO.getPayment() != null && requestDTO.getPayment().getPaymentType() != null) {
            isCOD = requestDTO.getPayment().getPaymentType() == Payment.PaymentType.COD;
        }
        if (order.getCoupon() != null && isCOD) {
            Coupon coupon = order.getCoupon();
            if (coupon.getUsageCount() == null) coupon.setUsageCount(0);
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemDTO.getProductVariantId()).orElse(null);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setVariant(variant);
            item.setQuantity(itemDTO.getQuantity());
            java.math.BigDecimal price = variant.getDiscountPrice() != null ? variant.getDiscountPrice() : variant.getPrice();
            item.setPrice(price);
            item.setPriceAtPurchase(itemDTO.getPrice());
            orderItems.add(orderItemRepository.save(item));
            // Trừ tồn kho chỉ nếu là COD/cash
            if (isCOD) {
                variant.setQuantityInStock(variant.getQuantityInStock() - itemDTO.getQuantity());
                productVariantRepository.save(variant);
            }
        }
        savedOrder.setOrderItems(orderItems);

        // Lưu Shipping_Address nếu có
        if (requestDTO.getShippingAddress() != null) {
            ShippingAddressDTO addrDTO = requestDTO.getShippingAddress();
            ShippingAddress address = new ShippingAddress();
            address.setOrder(savedOrder);
            address.setProvinceId(addrDTO.getProvinceId());
            address.setDistrictId(addrDTO.getDistrictId());
            address.setWardId(addrDTO.getWardId());
            address.setSpecificAddress(addrDTO.getSpecificAddress());
            address.setPhone(addrDTO.getPhone());
            address.setName(addrDTO.getName());
            address.setNote(addrDTO.getNote());
            shippingAddressRepository.save(address);
        }

        // Lưu Payment nếu có
        if (requestDTO.getPayment() != null) {
            PaymentDTO payDTO = requestDTO.getPayment();
            Payment payment = new Payment();
            payment.setOrder(savedOrder);
            
            // Validate và chuyển đổi payment type
            if (payDTO.getPaymentType() == null) {
                throw new IllegalArgumentException("Payment type không được để trống");
            }
            payment.setPaymentType(payDTO.getPaymentType());
            
            payment.setAmount(payDTO.getAmount());
            payment.setPaymentStatus(payDTO.getPaymentStatus());
            payment.setPaymentDate(payDTO.getPaymentDate());
            payment.setTransactionId(payDTO.getTransactionId());
            payment.setPaymentDetails(payDTO.getPaymentDetails());
            paymentRepository.save(payment);
        }

        // Lưu trạng thái đầu tiên
        OrderStatus status = new OrderStatus();
        status.setOrder(savedOrder);
        status.setStatusName("pending");
        status.setCreatedAt(LocalDateTime.now());
        orderStatusRepository.save(status);

        // Gửi email xác nhận đơn hàng cho khách hàng
        if (savedOrder.getCustomer() != null && savedOrder.getCustomer().getEmail() != null) {
            if (requestDTO.getPayment() != null && requestDTO.getPayment().getPaymentType() != null) {
                isCOD = requestDTO.getPayment().getPaymentType() == Payment.PaymentType.COD;
            }
            if (isCOD) {
                // Lấy ShippingAddressDTO (có tên tỉnh/huyện/xã)
                ShippingAddress address = shippingAddressRepository.findByOrder_OrderId(savedOrder.getOrderId());
                ShippingAddressDTO addrDTO = null;
                if (address != null) {
                    addrDTO = convertShippingAddressToDTO(address);
                }
                StringBuilder body = new StringBuilder();
                body.append("Xin chào ").append(savedOrder.getCustomer().getName()).append(",\n\n");
                body.append("Cảm ơn bạn đã đặt hàng tại TeeShirtVibe!\n");
                body.append("Mã đơn hàng: ").append(savedOrder.getOrderId()).append("\n");
                body.append("Ngày đặt: ").append(savedOrder.getCreatedAt()).append("\n");
                if (addrDTO != null) {
                    body.append("Địa chỉ nhận hàng: ")
                        .append(addrDTO.getSpecificAddress()).append(", ")
                        .append(addrDTO.getWardName()).append(", ")
                        .append(addrDTO.getDistrictName()).append(", ")
                        .append(addrDTO.getProvinceName()).append("\n");
                }
                body.append("\nDanh sách sản phẩm:\n");
                for (OrderItem item : orderItems) {
                    body.append("- ")
                        .append(item.getVariant().getProduct().getName())
                        .append(" (" + item.getVariant().getColor().getName() + ", " + item.getVariant().getSize().getName() + ")")
                        .append(", SL: ").append(item.getQuantity())
                        .append(", Giá: ").append(item.getPrice()).append("\n");
                }
                body.append("\nTổng tiền: ").append(savedOrder.getFinalAmount()).append(" VND\n");
                body.append("Phương thức thanh toán: ").append(requestDTO.getPayment() != null ? requestDTO.getPayment().getPaymentType() : "").append("\n");
                body.append("\nChúng tôi sẽ xử lý đơn hàng của bạn trong thời gian sớm nhất.\n");
                body.append("Trân trọng,\nTeeShirtVibe Team");
                emailService.sendSimpleEmail(savedOrder.getCustomer().getEmail(), "Xác nhận đơn hàng #" + savedOrder.getOrderId(), body.toString());
            }
        }

        return mapOrderToResponseDTO(savedOrder);
    }

    public List<OrderStatusDTO> getOrderStatusHistoryFullDTO(Long orderId, Long customerId) {
        Optional<Order> orderOpt = orderRepository.findByOrderIdAndCustomerIdWithStatuses(orderId, customerId);
        if (orderOpt.isEmpty()) return new java.util.ArrayList<>();
        Order order = orderOpt.get();
        List<OrderStatus> statuses = order.getOrderStatuses() != null ? order.getOrderStatuses() : new java.util.ArrayList<>();
        return statuses.stream().map(status -> {
            OrderStatusDTO dto = new OrderStatusDTO();
            dto.setOrderStatusId(status.getOrderStatusId());
            dto.setStatusName(status.getStatusName());
            dto.setCreatedAt(status.getCreatedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    // 2. Lấy chi tiết đơn hàng
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        return mapOrderToResponseDTO(order);
    }

    // 3. Lấy danh sách đơn hàng theo các tiêu chí
    public List<OrderResponseDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomer_CustomerId(customerId).stream().map(this::mapOrderToResponseDTO).collect(Collectors.toList());
    }
    public List<OrderResponseDTO> getOrdersByEmployee(Long employeeId) {
        return orderRepository.findByEmployee_EmployeeId(employeeId).stream().map(this::mapOrderToResponseDTO).collect(Collectors.toList());
    }
    public List<OrderResponseDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status).stream().map(this::mapOrderToResponseDTO).collect(Collectors.toList());
    }
    public List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByCreatedAtBetween(from, to).stream().map(this::mapOrderToResponseDTO).collect(Collectors.toList());
    }
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapOrderToResponseDTO).collect(Collectors.toList());
    }

    // --- GỘP CÁC HÀM LẤY ĐƠN HÀNG THEO CUSTOMER, STATUS, CHI TIẾT, HỦY ĐƠN ---
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }
    public List<OrderResponseDTO> getOrdersByCustomerAndStatus(Long customerId, String status) {
        List<Order> orders = orderRepository.findByCustomerCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
        return orders.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetail(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getCustomer().getCustomerId().equals(customerId)) {
            return null;
        }
        return convertToResponseDTO(order);
    }
    public boolean cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getCustomer().getCustomerId().equals(customerId)) {
            return false;
        }
        if (!"pending".equals(order.getStatus()) && !"processing".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("cancelled");
        orderRepository.save(order);
        return true;
    }
    // --- HÀM MAPPING DTO CHI TIẾT ---
    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);
        dto.setEmployeeId(order.getEmployee() != null ? order.getEmployee().getEmployeeId() : null);
        dto.setCouponId(order.getCoupon() != null ? order.getCoupon().getCouponId() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setOrderType(order.getOrderType());
        dto.setStatus(order.getStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        // Shipping Address
        ShippingAddress address = shippingAddressRepository.findByOrder_OrderId(order.getOrderId());
        if (address != null) {
            ShippingAddressDTO addrDTO = convertShippingAddressToDTO(address);
            dto.setShippingAddress(addrDTO);
        }
        // Order Items (detailed)
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderItemDTO> itemDTOs = items.stream().map(this::convertOrderItemToDTO).collect(Collectors.toList());
        dto.setItems(itemDTOs);
        // Status history (nếu có)
        List<OrderStatus> statusHistory = orderStatusRepository.findByOrder_OrderId(order.getOrderId());
        if (statusHistory != null) {
            List<OrderStatusDTO> statusDTOs = statusHistory.stream().map(s -> {
                OrderStatusDTO sDto = new OrderStatusDTO();
                sDto.setStatusName(s.getStatusName());
                sDto.setCreatedAt(s.getCreatedAt());
                return sDto;
            }).collect(Collectors.toList());
            dto.setStatusHistory(statusDTOs);
        }
        // Payment (nếu có)
        List<Payment> payments = paymentRepository.findByOrder_OrderId(order.getOrderId());
        if (payments != null && !payments.isEmpty()) {
            Payment payment = payments.get(0);
            PaymentDTO payDTO = new PaymentDTO();
            payDTO.setPaymentType(payment.getPaymentType());
            payDTO.setAmount(payment.getAmount());
            payDTO.setPaymentStatus(payment.getPaymentStatus());
            payDTO.setPaymentDate(payment.getPaymentDate());
            payDTO.setTransactionId(payment.getTransactionId());
            payDTO.setPaymentDetails(payment.getPaymentDetails());
            dto.setPayment(payDTO);
        }
        return dto;
    }
    private OrderItemDTO convertOrderItemToDTO(com.datn.teeshirt.Entity.OrderItem orderItem) {
        ProductVariantDTO variantDTO = null;
        if (orderItem.getVariant() != null) {
            com.datn.teeshirt.Entity.ProductVariant variant = orderItem.getVariant();
            String productName = variant.getProduct() != null ? variant.getProduct().getName() : "";
            String colorName = variant.getColor() != null ? variant.getColor().getName() : "";
            String sizeName = variant.getSize() != null ? variant.getSize().getName() : "";
            List<ProductImageDTO> images = null;
            if (variant.getProduct() != null && variant.getProduct().getImages() != null) {
                images = variant.getProduct().getImages().stream()
                        .map(img -> {
                            ProductImageDTO imgDto = new ProductImageDTO();
                            imgDto.setImageId(img.getImageId());
                            imgDto.setImageUrl(img.getImageUrl());
                            return imgDto;
                        })
                        .collect(Collectors.toList());
            }
            variantDTO = new ProductVariantDTO();
            variantDTO.setVariantId(variant.getVariantId());
            variantDTO.setProductId(variant.getProduct() != null ? variant.getProduct().getProductId() : null);
            variantDTO.setName(productName);
            variantDTO.setSku(variant.getSku());
            variantDTO.setColorId(variant.getColor() != null ? variant.getColor().getColorId() : null);
            variantDTO.setColorName(colorName);
            variantDTO.setSizeId(variant.getSize() != null ? variant.getSize().getSizeId() : null);
            variantDTO.setSizeName(sizeName);
            variantDTO.setPrice(variant.getPrice());
            variantDTO.setQuantityInStock(variant.getQuantityInStock());
            variantDTO.setIsActive(variant.getIsActive());
            variantDTO.setImages(images);
        }
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(orderItem.getOrderItemId());
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        dto.setVariant(variantDTO);
        return dto;
    }
    private ShippingAddressDTO convertShippingAddressToDTO(com.datn.teeshirt.Entity.ShippingAddress shippingAddress) {
        ShippingAddressDTO dto = new ShippingAddressDTO();
        dto.setProvinceId(shippingAddress.getProvinceId());
        dto.setDistrictId(shippingAddress.getDistrictId());
        dto.setWardId(shippingAddress.getWardId());
        dto.setSpecificAddress(shippingAddress.getSpecificAddress());
        dto.setPhone(shippingAddress.getPhone());
        dto.setName(shippingAddress.getName());
        dto.setNote(shippingAddress.getNote());
        
        // Lấy tên đầy đủ của địa chỉ
        populateAddressNames(dto);
        
        return dto;
    }
    
    // Hàm helper để lấy tên đầy đủ của địa chỉ
    @SuppressWarnings("unchecked")
    private void populateAddressNames(ShippingAddressDTO addrDTO) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Lấy tên tỉnh/thành
            if (addrDTO.getProvinceId() != null) {
                String provinceUrl = "https://provinces.open-api.vn/api/p/" + addrDTO.getProvinceId();
                Object provinceResponse = restTemplate.getForObject(provinceUrl, Object.class);
                if (provinceResponse instanceof Map) {
                    Map<String, Object> provinceData = (Map<String, Object>) provinceResponse;
                    addrDTO.setProvinceName((String) provinceData.get("name"));
                }
            }
            // Lấy tên quận/huyện và danh sách phường/xã
            if (addrDTO.getDistrictId() != null) {
                String districtUrl = "https://provinces.open-api.vn/api/d/" + addrDTO.getDistrictId() + "?depth=2";
                Object districtResponse = restTemplate.getForObject(districtUrl, Object.class);
                if (districtResponse instanceof Map) {
                    Map<String, Object> districtData = (Map<String, Object>) districtResponse;
                    addrDTO.setDistrictName((String) districtData.get("name"));
                    // Lấy tên phường/xã từ danh sách wards
                    if (addrDTO.getWardId() != null && districtData.get("wards") instanceof java.util.List) {
                        java.util.List<Map<String, Object>> wards = (java.util.List<Map<String, Object>>) districtData.get("wards");
                        for (Map<String, Object> wardMap : wards) {
                            if (addrDTO.getWardId().equals(String.valueOf(wardMap.get("code")))) {
                                addrDTO.setWardName((String) wardMap.get("name"));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi khi lấy tên địa chỉ, vẫn trả về địa chỉ với ID
            System.err.println("Error fetching address names: " + e.getMessage());
        }
    }

    // 4. Cập nhật trạng thái đơn hàng
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus, String note) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        // Kiểm tra điều kiện chuyển trạng thái nếu cần
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        // Lưu lịch sử trạng thái
        OrderStatus status = new OrderStatus();
        status.setOrder(order);
        status.setStatusName(newStatus);
        status.setCreatedAt(LocalDateTime.now());
        orderStatusRepository.save(status);
    }

    // 5. Thanh toán đơn hàng
    @Transactional
    public void payOrder(Long orderId, PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentType(paymentDTO.getPaymentType());
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentStatus("completed");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentDetails(paymentDTO.getPaymentDetails());
        paymentRepository.save(payment);
        // Cập nhật trạng thái đơn hàng nếu cần
        order.setStatus("paid");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        // Lưu lịch sử trạng thái
        OrderStatus status = new OrderStatus();
        status.setOrder(order);
        status.setStatusName("paid");
        status.setCreatedAt(LocalDateTime.now());
        orderStatusRepository.save(status);
        // Nếu có coupon, tăng usageCount (nếu chưa tăng)
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            if (coupon.getUsageCount() == null) coupon.setUsageCount(0);
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }
        // Trừ tồn kho cho từng sản phẩm (nếu chưa trừ)
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            ProductVariant variant = item.getVariant();
            if (variant != null) {
                variant.setQuantityInStock(variant.getQuantityInStock() - item.getQuantity());
                productVariantRepository.save(variant);
            }
        }
        // Gửi email xác nhận đơn hàng cho khách hàng
        if (order.getCustomer() != null && order.getCustomer().getEmail() != null) {
            ShippingAddress address = shippingAddressRepository.findByOrder_OrderId(order.getOrderId());
            ShippingAddressDTO addrDTO = null;
            if (address != null) {
                addrDTO = convertShippingAddressToDTO(address);
            }
            StringBuilder body = new StringBuilder();
            body.append("Xin chào ").append(order.getCustomer().getName()).append(",\n\n");
            body.append("Cảm ơn bạn đã đặt hàng tại TeeShirtVibe!\n");
            body.append("Mã đơn hàng: ").append(order.getOrderId()).append("\n");
            body.append("Ngày đặt: ").append(order.getCreatedAt()).append("\n");
            if (addrDTO != null) {
                body.append("Địa chỉ nhận hàng: ")
                    .append(addrDTO.getSpecificAddress()).append(", ")
                    .append(addrDTO.getWardName()).append(", ")
                    .append(addrDTO.getDistrictName()).append(", ")
                    .append(addrDTO.getProvinceName()).append("\n");
            }
            body.append("\nDanh sách sản phẩm:\n");
            for (OrderItem item : order.getOrderItems()) {
                body.append("- ")
                    .append(item.getVariant().getProduct().getName())
                    .append(" (" + item.getVariant().getColor().getName() + ", " + item.getVariant().getSize().getName() + ")")
                    .append(", SL: ").append(item.getQuantity())
                    .append(", Giá: ").append(item.getPrice()).append("\n");
            }
            body.append("\nTổng tiền: ").append(order.getFinalAmount()).append(" VND\n");
            body.append("Phương thức thanh toán: ").append(String.valueOf(paymentDTO.getPaymentType())).append("\n");
            body.append("\nChúng tôi sẽ xử lý đơn hàng của bạn trong thời gian sớm nhất.\n");
            body.append("Trân trọng,\nTeeShirtVibe Team");
            emailService.sendSimpleEmail(order.getCustomer().getEmail(), "Xác nhận đơn hàng #" + order.getOrderId(), body.toString());
        }
    }

    // 7. Thống kê, báo cáo
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }
    public Double getTotalRevenueByDateRangeAndStatus(LocalDateTime from, LocalDateTime to, String status) {
        return orderRepository.sumFinalAmountByDateRangeAndStatus(from, to, status);
    }
    public long countOrdersByDate(LocalDateTime date) {
        return orderRepository.countByDate(date);
    }

    // Tìm kiếm, lọc, phân trang đơn hàng
    public Page<OrderResponseDTO> searchOrders(Pageable pageable, String keyword, String status, String orderType, LocalDate createdDate) {
        Page<Order> page = orderRepository.searchOrders(keyword, status, orderType, createdDate, pageable);
        return page.map(this::mapOrderToResponseDTO);
    }

    // 8. Mapping Entity -> DTO
    private OrderResponseDTO mapOrderToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);
        dto.setEmployeeId(order.getEmployee() != null ? order.getEmployee().getEmployeeId() : null);
        dto.setCouponId(order.getCoupon() != null ? order.getCoupon().getCouponId() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setOrderType(order.getOrderType());
        dto.setStatus(order.getStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        // Shipping Address
        ShippingAddress address = shippingAddressRepository.findByOrder_OrderId(order.getOrderId());
        if (address != null) {
            ShippingAddressDTO addrDTO = convertShippingAddressToDTO(address);
            dto.setShippingAddress(addrDTO);
        }
        // Order Items (detailed)
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderItemDTO> itemDTOs = items.stream().map(this::convertOrderItemToDTO).collect(Collectors.toList());
        dto.setItems(itemDTOs);
        // Status History
        List<OrderStatus> statusList = orderStatusRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderStatusDTO> statusDTOs = statusList.stream().map(s -> {
            OrderStatusDTO st = new OrderStatusDTO();
            st.setStatusName(s.getStatusName());
            st.setCreatedAt(s.getCreatedAt());
            return st;
        }).collect(Collectors.toList());
        dto.setStatusHistory(statusDTOs);
        // Payment
        List<Payment> payments = paymentRepository.findByOrder_OrderId(order.getOrderId());
        if (!payments.isEmpty()) {
            Payment p = payments.get(0); // lấy payment đầu tiên (hoặc xử lý nhiều payment nếu cần)
            PaymentDTO payDTO = new PaymentDTO();
            payDTO.setPaymentType(p.getPaymentType());
            payDTO.setAmount(p.getAmount());
            payDTO.setPaymentStatus(p.getPaymentStatus());
            payDTO.setPaymentDate(p.getPaymentDate());
            payDTO.setTransactionId(p.getTransactionId());
            payDTO.setPaymentDetails(p.getPaymentDetails());
            dto.setPayment(payDTO);
        }
        return dto;
    }
}
