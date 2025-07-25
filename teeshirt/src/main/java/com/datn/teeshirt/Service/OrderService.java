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
import java.util.stream.Collectors;

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
    @Autowired private CustomerAddressRepository customerAddressRepository;

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

        // Nếu có coupon, tăng usageCount
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            if (coupon.getUsageCount() == null) coupon.setUsageCount(0);
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }

        // Lưu Order_Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemDTO.getProductVariantId()).orElse(null);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setVariant(variant);
            item.setQuantity(itemDTO.getQuantity());
            // Lấy giá giảm nếu có, nếu không lấy giá gốc
            java.math.BigDecimal price = variant.getDiscountPrice() != null ? variant.getDiscountPrice() : variant.getPrice();
            item.setPrice(price);
            item.setPriceAtPurchase(price);
            orderItems.add(orderItemRepository.save(item));
            // Trừ tồn kho
            variant.setQuantityInStock(variant.getQuantityInStock() - itemDTO.getQuantity());
            productVariantRepository.save(variant);
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

        return mapOrderToResponseDTO(savedOrder);
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
            ShippingAddressDTO addrDTO = new ShippingAddressDTO();
            addrDTO.setProvinceId(address.getProvinceId());
            addrDTO.setDistrictId(address.getDistrictId());
            addrDTO.setWardId(address.getWardId());
            addrDTO.setSpecificAddress(address.getSpecificAddress());
            addrDTO.setPhone(address.getPhone());
            addrDTO.setName(address.getName());
            addrDTO.setNote(address.getNote());
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
        return dto;
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
            ShippingAddressDTO addrDTO = new ShippingAddressDTO();
            addrDTO.setProvinceId(address.getProvinceId());
            addrDTO.setDistrictId(address.getDistrictId());
            addrDTO.setWardId(address.getWardId());
            addrDTO.setSpecificAddress(address.getSpecificAddress());
            addrDTO.setPhone(address.getPhone());
            addrDTO.setName(address.getName());
            addrDTO.setNote(address.getNote());
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
