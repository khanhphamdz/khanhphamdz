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

        // Lưu Order_Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemDTO.getProductVariantId()).orElse(null);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setVariant(variant);;
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            item.setPriceAtPurchase(itemDTO.getPrice());
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

    // 5. Hủy đơn hàng
    @Transactional
    public void cancelOrder(Long orderId, String note) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (order.getStatus().equals("cancelled")) {
            throw new IllegalStateException("Đơn hàng đã bị hủy trước đó");
        }
        // Hoàn kho
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);
        for (OrderItem item : items) {
            ProductVariant variant = item.getVariant();
            variant.setQuantityInStock(variant.getQuantityInStock() + item.getQuantity());
            productVariantRepository.save(variant);
        }
        // Cập nhật trạng thái
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        // Lưu lịch sử trạng thái
        OrderStatus status = new OrderStatus();
        status.setOrder(order);
        status.setStatusName("cancelled");
        status.setCreatedAt(LocalDateTime.now());
        orderStatusRepository.save(status);
    }

    // 6. Thanh toán đơn hàng
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
        // Order Items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderItemDTO> itemDTOs = items.stream().map(item -> {
            OrderItemDTO i = new OrderItemDTO();
            i.setProductVariantId(item.getVariant() != null ? item.getVariant().getVariantId() : null);
            i.setQuantity(item.getQuantity());
            i.setPrice(item.getPrice());
            i.setPrice(item.getPriceAtPurchase());
            return i;
        }).collect(Collectors.toList());
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
