package com.datn.teeshirt.Controller.API.AdminAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import com.datn.teeshirt.DTO.PromotionDTO;
import com.datn.teeshirt.DTO.CouponDTO;

import com.datn.teeshirt.Service.CouponService;
import com.datn.teeshirt.Service.PromotionService;

@RestController
@RequestMapping("/api/admin")
public class PromotionAPI {
    @Autowired
    PromotionService promotionService;

    @Autowired
    CouponService couponService;

    // --- Promotion ---
    @GetMapping("/promotion")
    public Page<PromotionDTO> getAllPromotions(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(name="keyword", required = false) String keyword) {
        return promotionService.getAll(page, size, keyword);
    }

    @GetMapping("/promotion/{id}")
    public PromotionDTO getPromotionById(@PathVariable Long id) {
        return promotionService.getById(id);
    }

    @PostMapping("/promotion")
    public PromotionDTO createPromotion(@RequestBody PromotionDTO dto) {
        return promotionService.create(dto);
    }

    @PutMapping("/promotion/{id}")
    public PromotionDTO updatePromotion(@PathVariable Long id, @RequestBody PromotionDTO dto) {
        return promotionService.update(id, dto);
    }
    
    @DeleteMapping("/promotion/{id}")
    public void deletePromotion(@PathVariable Long id) {
        promotionService.delete(id);
    }

    // --- Coupon ---
    @GetMapping("/coupon")
    public Page<CouponDTO> getAllCoupons(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(name="keyword", required = false) String keyword) {
        return couponService.getAll(page, size, keyword);
    }

    @PostMapping("/coupon")
    public CouponDTO createCoupon(@RequestBody CouponDTO dto) {
        return couponService.create(dto);
    }

    @PutMapping("/coupon/{id}")
    public CouponDTO updateCoupon(@PathVariable Long id, @RequestBody CouponDTO dto) {
        return couponService.update(id, dto);
    }

    @DeleteMapping("/coupon/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.delete(id);
    }

    // --- API thống kê cho dashboard ---
    @GetMapping("/promotions")
    public java.util.List<PromotionDTO> getAllPromotionsForStats() {
        // Lấy tất cả promotion không phân trang
        return promotionService.getAll(0, Integer.MAX_VALUE, null).getContent();
    }
    @GetMapping("/promotions/coupons")
    public java.util.List<CouponDTO> getAllCouponsForStats() {
        // Lấy tất cả coupon không phân trang
        return couponService.getAll(0, Integer.MAX_VALUE, null).getContent();
    }
}
