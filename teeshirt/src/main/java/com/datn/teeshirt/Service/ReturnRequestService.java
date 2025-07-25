package com.datn.teeshirt.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
=======
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

import com.datn.teeshirt.DTO.ReturnRequestDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.Order;
import com.datn.teeshirt.Entity.OrderItem;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Entity.ReturnRequest;
import com.datn.teeshirt.Entity.ReturnRequestItem;
import com.datn.teeshirt.Repository.CustomerRepository;
import com.datn.teeshirt.Repository.OrderItemRepository;
import com.datn.teeshirt.Repository.OrderRepository;
import com.datn.teeshirt.Repository.ProductImageRepository;
<<<<<<< HEAD
import com.datn.teeshirt.Repository.ProductVariantRepository;
import com.datn.teeshirt.Repository.ReturnRequestItemRepository;
import com.datn.teeshirt.Repository.ReturnRequestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
=======
import com.datn.teeshirt.Repository.ProductRepository;
import com.datn.teeshirt.Repository.ProductVariantRepository;
import com.datn.teeshirt.Repository.ReturnRequestItemRepository;
import com.datn.teeshirt.Repository.ReturnRequestRepository;
import com.datn.teeshirt.Repository.EmployeeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import com.datn.teeshirt.Entity.Employee;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

@Service
@Transactional
public class ReturnRequestService {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private ReturnRequestItemRepository returnRequestItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
<<<<<<< HEAD
=======
    private EmployeeRepository employeeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    private ProductVariantRepository productVariantRepository;

    public boolean createReturnRequest(ReturnRequestDTO returnRequestDTO, Long customerId) {
        try {
            // Kiểm tra order có tồn tại và thuộc về customer này không
            Optional<Order> orderOpt = orderRepository.findById(returnRequestDTO.getOrderId());
            if (!orderOpt.isPresent()) {
                return false;
            }

            Order order = orderOpt.get();

            // Kiểm tra order thuộc về customer này
            if (!order.getCustomer().getCustomerId().equals(customerId)) {
                return false;
            }

            // Kiểm tra order đã giao hay chưa
            if (!"delivered".equals(order.getStatus())) {
                return false;
            }

            // Kiểm tra xem đã có return request cho order này chưa
            List<ReturnRequest> existingReturns = returnRequestRepository
                    .findByCustomerIdOrderByRequestDateDesc(customerId);
            boolean hasExistingReturn = existingReturns.stream()
                    .anyMatch(r -> r.getOrder().getOrderId().equals(order.getOrderId()));
            if (hasExistingReturn) {
                return false; // Đã có return request rồi
            }

            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                return false;
            }

            // Tạo return request
            // Tính tổng số lượng sản phẩm hoàn trả
            int totalReturnQuantity = 0;
            if (returnRequestDTO.getReturnItems() != null) {
                for (ReturnRequestDTO.ReturnRequestItemDTO itemDTO : returnRequestDTO.getReturnItems()) {
                    if (itemDTO.getReturnQuantity() != null) {
                        totalReturnQuantity += itemDTO.getReturnQuantity();
                    }
                }
            }

<<<<<<< HEAD
=======
            // Parse returnType từ DTO (String)
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            ReturnRequest.ReturnType type = ReturnRequest.ReturnType.REFUND;
            if (returnRequestDTO.getReturnType() != null) {
                try {
                    type = ReturnRequest.ReturnType.valueOf(returnRequestDTO.getReturnType().toUpperCase());
<<<<<<< HEAD
                } catch (Exception ignored) {
                }
=======
                } catch (Exception ignored) {}
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            }
            ReturnRequest returnRequest = ReturnRequest.builder()
                    .order(order)
                    .customer(customer)
                    .returnReason(returnRequestDTO.getReturnReason())
                    .returnNote(returnRequestDTO.getReturnNote())
                    .returnImages(returnRequestDTO.getReturnImages())
                    .returnType(type)
                    .returnStatus(ReturnRequest.ReturnStatus.PENDING)
                    .requestDate(LocalDateTime.now())
                    .returnQuantity(totalReturnQuantity > 0 ? totalReturnQuantity : 1)
                    .build();

            // Tính tổng tiền hoàn trả
            BigDecimal totalRefundAmount = BigDecimal.ZERO;

            // Lưu return request trước
            returnRequest = returnRequestRepository.save(returnRequest);

            // Tạo return request items
            if (returnRequestDTO.getReturnItems() != null) {
                for (ReturnRequestDTO.ReturnRequestItemDTO itemDTO : returnRequestDTO.getReturnItems()) {
                    ProductVariant variant = productVariantRepository.findById(itemDTO.getVariantId()).orElse(null);
                    if (variant == null) {
                        continue;
                    }

                    // Tìm order item để lấy giá
                    OrderItem orderItem = order.getOrderItems().stream()
                            .filter(oi -> oi.getVariant().getVariantId().equals(itemDTO.getVariantId()))
                            .findFirst()
                            .orElse(null);

                    if (orderItem != null && itemDTO.getReturnQuantity() != null &&
                            itemDTO.getReturnQuantity() <= orderItem.getQuantity() && itemDTO.getReturnQuantity() > 0) {

                        // Xác định ProductCondition từ chuỗi
                        ReturnRequestItem.ProductCondition productCondition = ReturnRequestItem.ProductCondition.NEW;

                        if (itemDTO.getProductCondition() != null) {
                            try {
<<<<<<< HEAD
                                // Sửa: parse enum từ string (in hoa)
                                productCondition = ReturnRequestItem.ProductCondition
                                        .valueOf(itemDTO.getProductCondition().toUpperCase());
=======
                                // Thử parse enum từ string
                                productCondition = ReturnRequestItem.ProductCondition
                                        .valueOf(itemDTO.getProductCondition());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                            } catch (IllegalArgumentException e) {
                                // Nếu không parse được, mặc định là DAMAGED và ghi vào reason
                                productCondition = ReturnRequestItem.ProductCondition.DAMAGED;
                                String reason = itemDTO.getItemReturnReason();
                                if (reason == null) {
                                    reason = "Tình trạng khác: " + itemDTO.getProductCondition();
                                } else {
                                    reason += " | Tình trạng khác: " + itemDTO.getProductCondition();
                                }
                                itemDTO.setItemReturnReason(reason);
                            }
                        }

                        ReturnRequestItem returnRequestItem = ReturnRequestItem.builder()
                                .returnRequest(returnRequest)
                                .variant(variant)
                                .returnQuantity(itemDTO.getReturnQuantity())
                                .returnReason(itemDTO.getItemReturnReason())
                                .productCondition(productCondition) // Thêm tình trạng sản phẩm
                                .build();

                        returnRequestItemRepository.save(returnRequestItem);

                        // Tính tiền hoàn trả cho item này
                        BigDecimal itemRefundAmount = orderItem.getPriceAtPurchase()
                                .multiply(new BigDecimal(itemDTO.getReturnQuantity()));
                        totalRefundAmount = totalRefundAmount.add(itemRefundAmount);
                    }
                }
            }

            // Cập nhật tổng tiền hoàn trả
            returnRequest.setRefundAmount(totalRefundAmount);
            returnRequestRepository.save(returnRequest);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Overload cho multipart/form-data
<<<<<<< HEAD
    public boolean createReturnRequest(Long orderId, String reason, String description, String productsJson,
            List<MultipartFile> images, Long customerId, String returnType) {
        try {
            // Parse productsJson thành List<ReturnRequestDTO.ReturnRequestItemDTO>
            ObjectMapper mapper = new ObjectMapper();
            List<ReturnRequestDTO.ReturnRequestItemDTO> items = mapper.readValue(productsJson,
                    new TypeReference<List<ReturnRequestDTO.ReturnRequestItemDTO>>() {
                    });
=======
    public boolean createReturnRequest(Long orderId, String reason, String description, String productsJson, List<MultipartFile> images, Long customerId) {
        try {
            // Parse productsJson thành List<ReturnRequestDTO.ReturnRequestItemDTO>
            ObjectMapper mapper = new ObjectMapper();
            List<ReturnRequestDTO.ReturnRequestItemDTO> items = mapper.readValue(productsJson, new TypeReference<List<ReturnRequestDTO.ReturnRequestItemDTO>>(){});
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            ReturnRequestDTO dto = new ReturnRequestDTO();
            dto.setOrderId(orderId);
            dto.setReturnReason(reason);
            dto.setReturnNote(description);
            dto.setReturnItems(items);
<<<<<<< HEAD
            dto.setReturnType(returnType);
            // Xử lý ảnh nếu có
            if (images != null && !images.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (MultipartFile file : images) {
                    String fileName = file.getOriginalFilename();
                    sb.append("/images/return/").append(fileName).append(",");
                }
                if (sb.length() > 0)
                    sb.setLength(sb.length() - 1);
=======
            // Xử lý ảnh nếu có
            if (images != null && !images.isEmpty()) {
              
                StringBuilder sb = new StringBuilder();
                for (MultipartFile file : images) {
                    String fileName = file.getOriginalFilename();
                  
                    sb.append("/images/return/").append(fileName).append(",");
                }
                if (sb.length() > 0) sb.setLength(sb.length() - 1);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                dto.setReturnImages(sb.toString());
            }
            return createReturnRequest(dto, customerId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ReturnRequest> getReturnRequestsByCustomer(Long customerId) {
        return returnRequestRepository.findByCustomerIdOrderByRequestDateDesc(customerId);
    }

    // Trả về danh sách ReturnRequestDTO cho 1 customer
    public List<ReturnRequestDTO> getReturnRequestDTOsByCustomer(Long customerId) {
        List<ReturnRequest> entities = getReturnRequestsByCustomer(customerId);
        List<ReturnRequestDTO> dtos = new java.util.ArrayList<>();
        for (ReturnRequest entity : entities) {
            dtos.add(toDTO(entity));
        }
        return dtos;
    }

    // Chuyển đổi entity sang DTO
    public ReturnRequestDTO toDTO(ReturnRequest entity) {
<<<<<<< HEAD
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(entity.getOrder().getOrderId());
        dto.setReturnReason(entity.getReturnReason());
        dto.setReturnNote(entity.getReturnNote());
        dto.setReturnImages(entity.getReturnImages());
        dto.setReturnType(entity.getReturnType() != null ? entity.getReturnType().name() : null);
        dto.setReturnStatus(entity.getReturnStatus() != null ? entity.getReturnStatus().name() : null);
        dto.setRefundAmount(entity.getRefundAmount());
        dto.setRequestDate(entity.getRequestDate() != null ? entity.getRequestDate().toString() : null);

        // Chuyển đổi return items
        if (entity.getReturnRequestItems() != null) {
            List<ReturnRequestDTO.ReturnRequestItemDTO> itemDTOs = new java.util.ArrayList<>();
            for (ReturnRequestItem item : entity.getReturnRequestItems()) {
                ReturnRequestDTO.ReturnRequestItemDTO itemDTO = new ReturnRequestDTO.ReturnRequestItemDTO();
                itemDTO.setVariantId(item.getVariant().getVariantId());
                itemDTO.setReturnQuantity(item.getReturnQuantity());
                itemDTO.setItemReturnReason(item.getReturnReason());
                itemDTO.setProductCondition(
                        item.getProductCondition() != null ? item.getProductCondition().name() : null);
                itemDTOs.add(itemDTO);
            }
            dto.setReturnItems(itemDTOs);
        }

        return dto;
    }

    // Lấy return request theo orderId
    public ReturnRequestDTO getReturnRequestByOrderId(Long orderId, Long customerId) {
        List<ReturnRequest> returnRequests = returnRequestRepository.findByCustomerIdOrderByRequestDateDesc(customerId);
        for (ReturnRequest returnRequest : returnRequests) {
            if (returnRequest.getOrder().getOrderId().equals(orderId)) {
                return toDTO(returnRequest);
            }
        }
        return null;
    }

    // Hủy return request
    public boolean cancelReturnRequest(Long orderId, Long customerId) {
        List<ReturnRequest> returnRequests = returnRequestRepository.findByCustomerIdOrderByRequestDateDesc(customerId);
        for (ReturnRequest returnRequest : returnRequests) {
            if (returnRequest.getOrder().getOrderId().equals(orderId)) {
                // Chỉ cho phép hủy khi status là PENDING
                if (returnRequest.getReturnStatus() == ReturnRequest.ReturnStatus.PENDING) {
                    returnRequest.setReturnStatus(ReturnRequest.ReturnStatus.CANCELLED);
                    returnRequestRepository.save(returnRequest);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

=======
        if (entity == null) return null;
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setReturnId(entity.getReturnId());
        dto.setOrderId(entity.getOrder() != null ? entity.getOrder().getOrderId() : null);
        dto.setCustomerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null);
        dto.setReturnReason(entity.getReturnReason());
        dto.setReturnStatus(entity.getReturnStatus() != null ? entity.getReturnStatus().name() : null);
        dto.setReturnType(entity.getReturnType() != null ? entity.getReturnType().name() : null);
        dto.setRefundAmount(entity.getRefundAmount());
        dto.setRefundStatus(entity.getRefundStatus() != null ? entity.getRefundStatus().name() : null);
        dto.setReturnQuantity(entity.getReturnQuantity());
        dto.setRequestDate(entity.getRequestDate() != null ? entity.getRequestDate().toString() : null);
        dto.setReturnNote(entity.getReturnNote());
        dto.setReturnImages(entity.getReturnImages());
        // Map return items nếu cần
        // (Bổ sung nếu entity có getReturnRequestItems)
        return dto;
    }

>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    // Admin duyệt yêu cầu trả hàng
    public boolean approveReturnRequest(Long id, String adminNote) {
        Optional<ReturnRequest> opt = returnRequestRepository.findById(id);
        if (opt.isPresent()) {
            ReturnRequest req = opt.get();
            if (req.getReturnStatus() == ReturnRequest.ReturnStatus.PENDING) {
                // Cập nhật trạng thái return
                req.setReturnStatus(ReturnRequest.ReturnStatus.APPROVED);
                req.setAdminNote(adminNote);
                req.setProcessedDate(LocalDateTime.now());
<<<<<<< HEAD

=======
                
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                // Cập nhật trạng thái refund
                if (req.getReturnType() == ReturnRequest.ReturnType.REFUND) {
                    req.setRefundStatus(ReturnRequest.RefundStatus.PENDING);
                }
<<<<<<< HEAD

=======
                
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                returnRequestRepository.save(req);
                return true;
            }
        }
        return false;
    }

    // Admin từ chối yêu cầu trả hàng
    public boolean rejectReturnRequest(Long id, String reason) {
        Optional<ReturnRequest> opt = returnRequestRepository.findById(id);
        if (opt.isPresent()) {
            ReturnRequest req = opt.get();
            if (req.getReturnStatus() == ReturnRequest.ReturnStatus.PENDING) {
                req.setReturnStatus(ReturnRequest.ReturnStatus.REJECTED);
                req.setAdminNote(reason);
                req.setProcessedDate(LocalDateTime.now());
                returnRequestRepository.save(req);
                return true;
            }
        }
        return false;
    }

    // Hoàn tất yêu cầu trả hàng (sau khi đã hoàn tiền hoặc đổi hàng)
    public boolean completeReturnRequest(Long id, String adminNote) {
        Optional<ReturnRequest> opt = returnRequestRepository.findById(id);
        if (opt.isPresent()) {
            ReturnRequest req = opt.get();
            // Chỉ cho phép hoàn tất khi trạng thái là APPROVED
            if (req.getReturnStatus() != ReturnRequest.ReturnStatus.APPROVED) {
                return false;
            }
            req.setReturnStatus(ReturnRequest.ReturnStatus.COMPLETED);
            req.setCompletedDate(LocalDateTime.now());
            // Cập nhật trạng thái refund nếu là hoàn tiền
            if (req.getReturnType() == ReturnRequest.ReturnType.REFUND) {
                req.setRefundStatus(ReturnRequest.RefundStatus.COMPLETED);
            }
            if (adminNote != null && !adminNote.trim().isEmpty()) {
<<<<<<< HEAD
                req.setAdminNote(req.getAdminNote() != null ? req.getAdminNote() + " | " + adminNote : adminNote);
=======
                req.setAdminNote(req.getAdminNote() != null ?
                    req.getAdminNote() + " | " + adminNote : adminNote);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            }
            returnRequestRepository.save(req);
            return true;
        }
        return false;
    }

    // Admin hoặc khách hủy yêu cầu trả hàng
    public boolean cancelReturnRequest(Long id, String note) {
        Optional<ReturnRequest> opt = returnRequestRepository.findById(id);
        if (opt.isPresent()) {
            ReturnRequest req = opt.get();
            if (req.getReturnStatus() == ReturnRequest.ReturnStatus.PENDING) {
                req.setReturnStatus(ReturnRequest.ReturnStatus.CANCELLED);
                req.setAdminNote(note);
                req.setProcessedDate(LocalDateTime.now());
                returnRequestRepository.save(req);
                return true;
            }
        }
        return false;
    }

    // Lấy danh sách yêu cầu trả hàng (lọc theo trạng thái)
    public List<ReturnRequest> getReturnRequests(String status) {
        if (status == null || status.isEmpty()) {
            return returnRequestRepository.findAll();
        }
        try {
            ReturnRequest.ReturnStatus st = ReturnRequest.ReturnStatus.valueOf(status.toUpperCase());
            return returnRequestRepository.findByReturnStatusOrderByRequestDateDesc(st);
        } catch (Exception e) {
            return returnRequestRepository.findAll();
        }
    }

    // Lấy chi tiết yêu cầu trả hàng
    public ReturnRequest getReturnRequestDetail(Long id) {
        return returnRequestRepository.findById(id).orElse(null);
    }

    public List<ReturnRequestDTO> getReturnRequestDTOs(String status) {
        List<ReturnRequest> entities = getReturnRequests(status);
        List<ReturnRequestDTO> dtos = new java.util.ArrayList<>();
        for (ReturnRequest entity : entities) {
            dtos.add(toDTO(entity));
        }
        return dtos;
    }

    public ReturnRequestDTO getReturnRequestDTODetail(Long id) {
        ReturnRequest entity = getReturnRequestDetail(id);
<<<<<<< HEAD
        if (entity == null)
            return null;
=======
        if (entity == null) return null;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        return toDTO(entity);
    }

    /**
     * Lấy chi tiết yêu cầu trả hàng đầy đủ cho giao diện quản trị
     */
    public java.util.Map<String, Object> getReturnRequestDetailFull(Long id) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        ReturnRequest rr = getReturnRequestDetail(id);
<<<<<<< HEAD
        if (rr == null)
            return null;
=======
        if (rr == null) return null;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        // Thông tin cơ bản
        result.put("returnId", rr.getReturnId());
        result.put("orderId", rr.getOrder().getOrderId());
        result.put("customerId", rr.getCustomer().getCustomerId());
        result.put("customerName", rr.getCustomer().getName());
        result.put("customerPhone", rr.getCustomer().getPhone());
        result.put("customerEmail", rr.getCustomer().getEmail());
        result.put("requestDate", rr.getRequestDate());
        result.put("returnReason", rr.getReturnReason());
        result.put("refundAmount", rr.getRefundAmount());
        result.put("returnStatus", rr.getReturnStatus() != null ? rr.getReturnStatus().name() : null);
        result.put("returnType", rr.getReturnType() != null ? rr.getReturnType().name() : null);
        result.put("refundStatus", rr.getRefundStatus() != null ? rr.getRefundStatus().name() : null);
        result.put("returnQuantity", rr.getReturnQuantity());
        result.put("returnImages", rr.getReturnImages());
        result.put("returnNote", rr.getReturnNote());
        result.put("adminNote", rr.getAdminNote());
        result.put("processedDate", rr.getProcessedDate());
        result.put("completedDate", rr.getCompletedDate());
        // Nhân viên xử lý
        if (rr.getProcessedBy() != null) {
            result.put("processedBy", rr.getProcessedBy().getFullName());
        }
        // Thông tin đơn hàng
        Order order = rr.getOrder();
        result.put("orderDate", order.getCreatedAt());
        result.put("orderStatus", order.getStatus());
        result.put("orderType", order.getOrderType());
        result.put("orderTotal", order.getTotalAmount());
        result.put("orderFinalAmount", order.getFinalAmount());
        // Sản phẩm hoàn trả (theo từng item)
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
<<<<<<< HEAD
        java.util.List<ReturnRequestItem> returnItems = returnRequestItemRepository
                .findByReturnRequestId(rr.getReturnId());
=======
        java.util.List<ReturnRequestItem> returnItems = returnRequestItemRepository.findByReturnRequestId(rr.getReturnId());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        for (ReturnRequestItem item : returnItems) {
            java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
            ProductVariant variant = item.getVariant();
            Product product = variant.getProduct();
            itemMap.put("productName", product.getName());
            itemMap.put("variantName", variant.getName());
            itemMap.put("color", variant.getColor() != null ? variant.getColor().getName() : null);
            itemMap.put("size", variant.getSize() != null ? variant.getSize().getName() : null);
            itemMap.put("returnQuantity", item.getReturnQuantity());
<<<<<<< HEAD
            itemMap.put("productCondition",
                    item.getProductCondition() != null ? item.getProductCondition().name() : null);
=======
            itemMap.put("productCondition", item.getProductCondition() != null ? item.getProductCondition().name() : null);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            itemMap.put("returnReason", item.getReturnReason());
            // Lấy giá mua từ OrderItem
            java.util.List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(order.getOrderId());
            java.math.BigDecimal price = null;
            for (OrderItem oi : orderItems) {
                if (oi.getVariant().getVariantId().equals(variant.getVariantId())) {
                    price = oi.getPriceAtPurchase();
                    break;
                }
            }
            itemMap.put("priceAtPurchase", price);
            // Hình ảnh sản phẩm (lấy theo variant, nếu không có thì lấy theo product)
            java.util.List<String> images = new java.util.ArrayList<>();
<<<<<<< HEAD
            java.util.List<com.datn.teeshirt.Entity.ProductImage> variantImgs = productImageRepository
                    .findByVariant_VariantId(variant.getVariantId());
=======
            java.util.List<com.datn.teeshirt.Entity.ProductImage> variantImgs = productImageRepository.findByVariant_VariantId(variant.getVariantId());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            if (variantImgs != null && !variantImgs.isEmpty()) {
                for (com.datn.teeshirt.Entity.ProductImage img : variantImgs) {
                    images.add(img.getImageUrl());
                }
            } else {
<<<<<<< HEAD
                java.util.List<com.datn.teeshirt.Entity.ProductImage> productImgs = productImageRepository
                        .findByProduct_ProductId(product.getProductId());
=======
                java.util.List<com.datn.teeshirt.Entity.ProductImage> productImgs = productImageRepository.findByProduct_ProductId(product.getProductId());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                for (com.datn.teeshirt.Entity.ProductImage img : productImgs) {
                    images.add(img.getImageUrl());
                }
            }
            itemMap.put("images", images);
            items.add(itemMap);
        }
        result.put("returnItems", items);
        // (Có thể bổ sung lịch sử xử lý, liên hệ... nếu có bảng tương ứng)
        return result;
    }

    public long countByStatus(String status) {
        try {
            ReturnRequest.ReturnStatus st = ReturnRequest.ReturnStatus.valueOf(status.toUpperCase());
            return returnRequestRepository.countByReturnStatus(st);
        } catch (Exception e) {
            return 0;
        }
    }

    public Page<ReturnRequestDTO> searchAndPage(String status, String keyword, Pageable pageable) {
        ReturnRequest.ReturnStatus st = null;
        if (status != null && !status.isEmpty()) {
            try {
                st = ReturnRequest.ReturnStatus.valueOf(status.toUpperCase());
<<<<<<< HEAD
            } catch (Exception ignored) {
            }
=======
            } catch (Exception ignored) {}
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        }
        Page<ReturnRequest> page = returnRequestRepository.searchByStatusAndKeyword(st, keyword, pageable);
        return page.map(this::toDTO);
    }
<<<<<<< HEAD
}
=======
}

>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
