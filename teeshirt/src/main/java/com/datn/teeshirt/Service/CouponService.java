package com.datn.teeshirt.Service;

import com.datn.teeshirt.DTO.CouponDTO;
import com.datn.teeshirt.Entity.Coupon;
import com.datn.teeshirt.Repository.CouponRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
<<<<<<< HEAD
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.security.core.Authentication;
import java.util.List;
=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    // Lấy tất cả coupon (có phân trang, tìm kiếm)
    public Page<CouponDTO> getAll(int page, int size, String code) {
        Pageable pageable = PageRequest.of(page, size);
        if (code != null && !code.trim().isEmpty()) {
            return couponRepository.findByCodeContainingIgnoreCase(code).stream()
                    .map(this::toDTO)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        }
        return couponRepository.findAll(pageable).map(this::toDTO);
    }

    // Lấy coupon theo ID
    public CouponDTO getById(Long id) {
        return couponRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // Tạo mới coupon
    @Transactional
    public CouponDTO create(CouponDTO dto) {
<<<<<<< HEAD
        Coupon coupon = toEntity(dto);
        coupon.setUsageCount(dto.getUsageCount() != null ? dto.getUsageCount() : 0);
        coupon.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
=======
        // Check hạn mức %
        if ("percentage".equalsIgnoreCase(dto.getType())) {
            if (dto.getDiscountValue() == null || dto.getDiscountValue().doubleValue() <= 0 || dto.getDiscountValue().doubleValue() > 99) {
                throw new IllegalArgumentException("Giá trị phần trăm giảm giá phải lớn hơn 0 và không vượt quá 99%!");
            }
        }
        Coupon coupon = toEntity(dto);
        coupon.setUsageCount(dto.getUsageCount() != null ? dto.getUsageCount() : 0);
        coupon.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        // Tự động xác định type nếu chưa set
        if (dto.getType() == null || dto.getType().isEmpty()) {
            if (dto.getDiscountValue() != null && dto.getDiscountValue().doubleValue() <= 1.0) {
                coupon.setType("percentage");
            } else {
                coupon.setType("fixed");
            }
        } else {
            coupon.setType(dto.getType());
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        return toDTO(couponRepository.save(coupon));
    }

    // Cập nhật coupon
    @Transactional
    public CouponDTO update(Long id, CouponDTO dto) {
<<<<<<< HEAD
        Coupon coupon = couponRepository.findById(id).orElseThrow();
        boolean hasUsage = coupon.getUsageCount() != null && coupon.getUsageCount() > 0;
        if (!hasUsage) {
            // Nếu chưa có ai sử dụng, cho phép cập nhật tất cả các trường
            coupon.setCode(dto.getCode());
            coupon.setType(dto.getType());
            coupon.setDiscountValue(dto.getDiscountValue());
        }
        // Các trường cơ bản luôn cho phép cập nhật
        coupon.setDescription(dto.getDescription());
=======
        // Check hạn mức %
        if ("percentage".equalsIgnoreCase(dto.getType())) {
            if (dto.getDiscountValue() == null || dto.getDiscountValue().doubleValue() <= 0 || dto.getDiscountValue().doubleValue() > 99) {
                throw new IllegalArgumentException("Giá trị phần trăm giảm giá phải lớn hơn 0 và không vượt quá 99%!");
            }
        }
        Coupon coupon = couponRepository.findById(id).orElseThrow();
        coupon.setCode(dto.getCode());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountValue(dto.getDiscountValue());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setMaxUsage(dto.getMaxUsage());
        coupon.setUsageCount(dto.getUsageCount());
        coupon.setIsActive(dto.getIsActive());
        coupon.setApplyToCustomer(dto.getApplyToCustomer());
<<<<<<< HEAD
=======
        // Tự động xác định type nếu chưa set
        if (dto.getType() == null || dto.getType().isEmpty()) {
            if (dto.getDiscountValue() != null && dto.getDiscountValue().doubleValue() <= 1.0) {
                coupon.setType("percentage");
            } else {
                coupon.setType("fixed");
            }
        } else {
            coupon.setType(dto.getType());
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        return toDTO(couponRepository.save(coupon));
    }

    // Xóa coupon
    @Transactional
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    // Đổi trạng thái active/inactive
    @Transactional
    public CouponDTO setActive(Long id, boolean active) {
        Coupon coupon = couponRepository.findById(id).orElseThrow();
        coupon.setIsActive(active);
        return toDTO(couponRepository.save(coupon));
    }

<<<<<<< HEAD
    // Kiểm tra hợp lệ mã giảm giá
    public CouponDTO validateCoupon(String code, Long customerId, java.math.BigDecimal orderValue, boolean isNewCustomer) {
        Coupon coupon = couponRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

        if (!coupon.getIsActive()) throw new IllegalArgumentException("Mã giảm giá đã bị khóa");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (coupon.getStartDate().isAfter(now) || coupon.getEndDate().isBefore(now))
            throw new IllegalArgumentException("Mã giảm giá hết hạn hoặc chưa bắt đầu");
        if (coupon.getMaxUsage() != null && coupon.getUsageCount() >= coupon.getMaxUsage())
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
        if (coupon.getMinOrderValue() != null && orderValue.compareTo(coupon.getMinOrderValue()) < 0)
            throw new IllegalArgumentException("Đơn hàng chưa đủ điều kiện áp dụng mã giảm giá");

        // Kiểm tra loại khách hàng
        switch (coupon.getApplyToCustomer()) {
            case "all":
                break;
            case "new":
                if (!isNewCustomer) throw new IllegalArgumentException("Mã chỉ áp dụng cho khách hàng mới");
                break;
            case "specific":
                // TODO: kiểm tra customerId có nằm trong danh sách được phép không
                break;
        }
        return toDTO(coupon);
    }

    // Tăng usage_count khi đơn hàng thành công
    @Transactional
    public void increaseUsage(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
    }

    // Giảm usage_count khi đơn hàng bị hủy
    @Transactional
    public void decreaseUsage(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        coupon.setUsageCount(Math.max(0, coupon.getUsageCount() - 1));
        couponRepository.save(coupon);
    }

    // Lấy coupon theo code
    public CouponDTO getCouponByCode(String code) {
        return couponRepository.findByCode(code).map(this::toDTO).orElse(null);
    }

    // Validate và tính toán discount
    public java.util.Map<String, Object> validateAndCalculateDiscount(String code, java.math.BigDecimal totalAmount) {
        try {
            Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

            if (!coupon.getIsActive()) {
                throw new IllegalArgumentException("Mã giảm giá đã bị khóa");
            }

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (coupon.getStartDate().isAfter(now) || coupon.getEndDate().isBefore(now)) {
                throw new IllegalArgumentException("Mã giảm giá hết hạn hoặc chưa bắt đầu");
            }

            if (coupon.getMaxUsage() != null && coupon.getUsageCount() >= coupon.getMaxUsage()) {
                throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
            }

            if (coupon.getMinOrderValue() != null && totalAmount.compareTo(coupon.getMinOrderValue()) < 0) {
                throw new IllegalArgumentException("Đơn hàng chưa đủ điều kiện áp dụng mã giảm giá (tối thiểu " + 
                    coupon.getMinOrderValue().toPlainString() + " VNĐ)");
            }

            // Tính toán discount amount
            java.math.BigDecimal discountAmount;
            if (coupon.getDiscountValue().toString().contains("%")) {
                // Giảm theo phần trăm
                String percentStr = coupon.getDiscountValue().toString().replace("%", "");
                java.math.BigDecimal percent = new java.math.BigDecimal(percentStr);
                discountAmount = totalAmount.multiply(percent).divide(new java.math.BigDecimal("100"));
            } else {
                // Giảm theo số tiền cố định
                discountAmount = coupon.getDiscountValue();
            }

            // Đảm bảo discount không vượt quá totalAmount
            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("couponId", coupon.getCouponId());
            result.put("code", coupon.getCode());
            result.put("description", coupon.getDescription());
            result.put("discountAmount", discountAmount);
            result.put("finalAmount", totalAmount.subtract(discountAmount));
            result.put("discountType", coupon.getDiscountValue().toString().contains("%") ? "percent" : "fixed");

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    // Đếm tổng số coupon
    public long countAll() {
        return couponRepository.count();
    }

    // Đếm số coupon đang hoạt động (isActive = true, ngày hiện tại nằm trong startDate-endDate)
    public long countActive() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive())
                && (c.getStartDate() == null || !now.isBefore(c.getStartDate()))
                && (c.getEndDate() == null || !now.isAfter(c.getEndDate())))
            .count();
    }

    // Đếm tổng usageCount tăng trong tháng hiện tại
    public long countUsageThisMonth() {
        YearMonth thisMonth = YearMonth.now();
        return couponRepository.findAll().stream()
            .mapToLong(c -> {
                // Nếu có updatedAt trong tháng này thì tính usageCount
                if (c.getUpdatedAt() != null && YearMonth.from(c.getUpdatedAt()).equals(thisMonth)) {
                    return c.getUsageCount() != null ? c.getUsageCount() : 0;
                }
                return 0;
            }).sum();
    }

    // Lấy danh sách mã giảm giá khả dụng cho khách hàng hiện tại
    public List<CouponDTO> getAvailableCouponsForCustomer(Authentication authentication) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return couponRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive())
                && (c.getStartDate() == null || !now.isBefore(c.getStartDate()))
                && (c.getEndDate() == null || !now.isAfter(c.getEndDate()))
                && (c.getMaxUsage() == null || c.getUsageCount() < c.getMaxUsage()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

=======
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    // Chuyển đổi Entity -> DTO
    private CouponDTO toDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setCouponId(coupon.getCouponId());
        dto.setCode(coupon.getCode());
        dto.setDescription(coupon.getDescription());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMinOrderValue(coupon.getMinOrderValue());
        dto.setStartDate(coupon.getStartDate());
        dto.setEndDate(coupon.getEndDate());
        dto.setMaxUsage(coupon.getMaxUsage());
        dto.setUsageCount(coupon.getUsageCount());
        dto.setIsActive(coupon.getIsActive());
        dto.setApplyToCustomer(coupon.getApplyToCustomer());
        dto.setType(coupon.getType());
        return dto;
    }

    // Chuyển đổi DTO -> Entity
    private Coupon toEntity(CouponDTO dto) {
        return Coupon.builder()
                .couponId(dto.getCouponId())
                .code(dto.getCode())
                .description(dto.getDescription())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .maxUsage(dto.getMaxUsage())
                .usageCount(dto.getUsageCount())
                .isActive(dto.getIsActive())
                .applyToCustomer(dto.getApplyToCustomer())
<<<<<<< HEAD
                .type(dto.getType())
=======
                .type(dto.getType() != null ? dto.getType() : "fixed")
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                .build();
    }
} 