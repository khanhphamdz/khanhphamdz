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
import com.datn.teeshirt.Service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

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
        return ResponseEntity.ok(new ResponseObject("ok", "Thêm sản phẩm vào giỏ thành công", null));
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
}