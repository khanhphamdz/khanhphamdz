package com.datn.teeshirt.Controller.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Security.CustomUserDetailsService;
import com.datn.teeshirt.Service.WishlistService;


@RestController
@RequestMapping("/account/wishlist")
public class WishlistAPI {

    @Autowired
    WishlistService wishlistService;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @GetMapping("/add")
    public ResponseEntity<ResponseObject> addToWishlist(@RequestParam Long productId) {
        try {
            Customer customer = customUserDetailsService.getCustomerInfo();
            wishlistService.addProductToWishlist(customer, productId);
            return ResponseEntity.ok(new ResponseObject("ok", "Thêm sản phẩm vào yêu thích thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra khi thêm sản phẩm vào yêu thích", e.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResponseObject> removeFromWishlist(@RequestParam Long productId) {
        try {
            Customer customer = customUserDetailsService.getCustomerInfo();
            wishlistService.removeProductFromWishlist(customer, productId);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa sản phẩm khỏi yêu thích thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra khi xóa sản phẩm khỏi    yêu thích", e.getMessage()));
        }
    }
    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getWishlistItems() {
        try {
            Customer customer = customUserDetailsService.getCustomerInfo();
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy danh sách sản phẩm yêu thích thành công", wishlistService.getWishlistItems(customer)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Có lỗi xảy ra khi lấy danh sách sản phẩm yêu thích", e.getMessage()));
        }
    }
}
