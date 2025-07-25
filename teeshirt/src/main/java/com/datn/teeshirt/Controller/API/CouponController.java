package com.datn.teeshirt.Controller.API;

import com.datn.teeshirt.Entity.Coupon;
import com.datn.teeshirt.Repository.CouponRepository;
import com.datn.teeshirt.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private OrderRepository orderRepository;

    // API kiểm tra mã giảm giá
    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code, @RequestParam(required = false) Double total, @RequestParam(required = false) Long userId) {
        Coupon coupon = couponRepository.findByCode(code).orElse(null);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (coupon == null || !Boolean.TRUE.equals(coupon.getIsActive())) {
            return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Mã giảm giá không hợp lệ!"));
        }
        // Check thời gian còn hiệu lực
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Phiếu giảm giá đã hết hạn hoặc chưa đến thời gian sử dụng!"));
        }
        // Check tồn kho (giả lập, nếu cần check thực tế thì cần truyền thêm thông tin giỏ hàng)
        // Check user đã dùng chưa (nếu cần check thực tế thì cần truyền userId và lưu lịch sử sử dụng coupon)
        if (coupon.getMaxUsage() != null && coupon.getUsageCount() != null && coupon.getUsageCount() >= coupon.getMaxUsage()) {
            return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Phiếu giảm giá đã hết lượt sử dụng!"));
        }
        double orderTotal = total != null ? total : 0;
        // Check điều kiện giỏ hàng
        if (coupon.getMinOrderValue() != null && orderTotal < coupon.getMinOrderValue().doubleValue()) {
            return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Đơn hàng chưa đủ giá trị tối thiểu!"));
        }
        String type = coupon.getType() != null ? coupon.getType() : "fixed";
        double discount = 0;
        if ("percentage".equalsIgnoreCase(type)) {
            // Không cho giảm quá 99%
            double percent = coupon.getDiscountValue().doubleValue();
            if (percent > 99) percent = 99;
            discount = orderTotal * (percent / 100.0);
        } else {
            discount = coupon.getDiscountValue().doubleValue();
            // Nếu là mã cố định mà lớn hơn orderTotal thì báo lỗi
            if (discount > orderTotal) {
                return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Mã giảm giá không hợp lệ với giá trị đơn hàng hiện tại!"));
            }
        }
        // Không cho giảm quá giá trị đơn hàng
        if (discount > orderTotal) discount = orderTotal;
        // Không cho giảm quá 99% giá trị đơn hàng
        if (discount > orderTotal * 0.99) discount = Math.floor(orderTotal * 0.99);
        if (orderTotal - discount < 0) {
            return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Giá trị đơn hàng sau giảm không hợp lệ!"));
        }
        // Check user đã dùng chưa (nếu cần, cần truyền userId và kiểm tra lịch sử đơn hàng)
        if (userId != null) {
            long usedCount = orderRepository.countByCustomerIdAndCouponId(userId, coupon.getCouponId());
            if (usedCount > 0) {
                return ResponseEntity.ok(java.util.Map.of("valid", false, "message", "Bạn đã sử dụng mã giảm giá này rồi!"));
            }
        }
        return ResponseEntity.ok(java.util.Map.of(
            "valid", true,
            "couponId", coupon.getCouponId(),
            "discountAmount", discount,
            "type", type,
            "message", "Áp dụng thành công: Giảm " + ("percentage".equalsIgnoreCase(type) ? (coupon.getDiscountValue().doubleValue() + "%") : (discount + "đ"))
        ));
    }
} 