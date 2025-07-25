package com.datn.teeshirt.Controller.API.AdminAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import com.datn.teeshirt.DTO.CouponDTO;
import com.datn.teeshirt.Service.CouponService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
public class CouponController {
    @Autowired
    CouponService couponService;

    @GetMapping("")
    public Page<CouponDTO> getAllCoupons(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(name="keyword", required = false) String keyword) {
        return couponService.getAll(page, size, keyword);
    }

    @GetMapping("/{id}")
    public CouponDTO getCouponById(@PathVariable Long id) {
        return couponService.getById(id);
    }

    @PostMapping("")
    public CouponDTO createCoupon(@RequestBody CouponDTO dto) {
        return couponService.create(dto);
    }

    @PutMapping("/{id}")
    public CouponDTO updateCoupon(@PathVariable Long id, @RequestBody CouponDTO dto) {
        return couponService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.delete(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> getCouponStats() {
        Map<String, Object> stats = new HashMap<>();
        // Tổng số phiếu giảm giá
        long totalCoupons = couponService.countAll();
        // Số phiếu giảm giá đang hoạt động (isActive = true, ngày hiện tại nằm trong startDate-endDate)
        long activeCoupons = couponService.countActive();
        // Số chương trình đang diễn ra (giả sử là số promotion đang active, nếu có PromotionService thì lấy từ đó, tạm để 0)
        long activePrograms = 0L;
        // Lượt sử dụng tháng này (tổng usageCount tăng trong tháng hiện tại)
        long usageThisMonth = couponService.countUsageThisMonth();
        stats.put("activePrograms", activePrograms);
        stats.put("activeCoupons", activeCoupons);
        stats.put("totalCoupons", totalCoupons);
        stats.put("usageThisMonth", usageThisMonth);
        return stats;
    }
} 