package com.datn.teeshirt.Controller.Client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.Service.WishlistService;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    @GetMapping
    public String wishlistPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        List<ProductDTO> wishlistProducts = wishlistService.getWishlistProducts(authentication);
        model.addAttribute("wishlistProducts", wishlistProducts);
        
        return "customer/product/wishlist";
    }
    
    @PostMapping("/add/{productId}")
    @ResponseBody
    public String addToWishlist(@PathVariable Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "error:Vui lòng đăng nhập";
        }
        
        try {
            wishlistService.addToWishlist(productId, authentication);
            return "success:Đã thêm vào danh sách yêu thích";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
    
    @PostMapping("/remove/{productId}")
    @ResponseBody
    public String removeFromWishlist(@PathVariable Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "error:Vui lòng đăng nhập";
        }
        
        try {
            wishlistService.removeFromWishlist(productId, authentication);
            return "success:Đã xóa khỏi danh sách yêu thích";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
    
    @GetMapping("/check/{productId}")
    @ResponseBody
    public boolean isInWishlist(@PathVariable Long productId, Authentication authentication) {
        return wishlistService.isProductInWishlist(productId, authentication);
    }
    
    @GetMapping("/count")
    @ResponseBody
    public int getWishlistCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        return wishlistService.getWishlistCount(authentication);
    }
} 