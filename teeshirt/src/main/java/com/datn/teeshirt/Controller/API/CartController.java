package com.datn.teeshirt.Controller.API;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.CartItemDTO;
import com.datn.teeshirt.DTO.CartItemRequest;
import com.datn.teeshirt.DTO.CouponDTO;
import com.datn.teeshirt.Service.CartService;
import com.datn.teeshirt.Service.CouponService;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private CouponService couponService;

    // Lấy giỏ hàng của user
    @GetMapping("")
    public ResponseEntity<ResponseObject> getCart(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseObject("error", "Bạn chưa đăng nhập", null));
        }
        List<CartItemDTO> cartItems = cartService.getCartItems(authentication);

        return ResponseEntity.ok(new ResponseObject("ok", "Lấy giỏ hàng thành công", cartItems));
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<ResponseObject> addToCart(@RequestBody CartItemRequest request,
            Authentication authentication) {
        cartService.addToCart(request, authentication);
        return ResponseEntity.ok(new ResponseObject("ok", "Thêm sản phẩm vào giỏ thành công", request));
    }

    // Cập nhật số lượng sản phẩm trong giỏ
    @PutMapping("/update")
    public ResponseEntity<ResponseObject> updateCartItem(@RequestBody CartItemRequest request,
            Authentication authentication) {
        cartService.updateCartItem(request, authentication);
        return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật số lượng thành công", null));
    }

    // Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<ResponseObject> removeCartItem(@PathVariable Long variantId, Authentication authentication) {
        try {
            cartService.removeCartItem(variantId, authentication);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa sản phẩm khỏi giỏ thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("error", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject("error", "Có lỗi xảy ra khi xóa sản phẩm khỏi giỏ hàng", e.getMessage()));
        }
    }

    // Xóa nhiều sản phẩm khỏi giỏ hàng (sau khi đặt hàng thành công)
    @DeleteMapping("/remove-multiple")
    public ResponseEntity<ResponseObject> removeMultipleCartItems(@RequestBody List<Long> variantIds, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseObject("error", "Bạn chưa đăng nhập", null));
            }
            
            cartService.removeMultipleCartItems(variantIds, authentication);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa sản phẩm khỏi giỏ thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject("error", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject("error", "Có lỗi xảy ra khi xóa sản phẩm khỏi giỏ hàng", e.getMessage()));
        }
    }

    // API lấy danh sách mã giảm giá khả dụng cho khách hàng hiện tại
    @GetMapping("/coupons/available")
    public ResponseEntity<ResponseObject> getAvailableCoupons(Authentication authentication) {
        try {
            // Lấy các coupon đang active, còn hạn, còn lượt sử dụng
            List<CouponDTO> availableCoupons = couponService.getAvailableCouponsForCustomer(authentication);
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy mã giảm giá thành công", availableCoupons));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("error", "Có lỗi khi lấy mã giảm giá", null));
        }
    }
}