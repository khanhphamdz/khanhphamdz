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
        return toDTO(couponRepository.save(coupon));
    }

    // Cập nhật coupon
    @Transactional
    public CouponDTO update(Long id, CouponDTO dto) {
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
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setMaxUsage(dto.getMaxUsage());
        coupon.setUsageCount(dto.getUsageCount());
        coupon.setIsActive(dto.getIsActive());
        coupon.setApplyToCustomer(dto.getApplyToCustomer());
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
                .type(dto.getType() != null ? dto.getType() : "fixed")
                .build();
    }
} 