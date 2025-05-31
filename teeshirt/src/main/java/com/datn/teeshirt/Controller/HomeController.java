package com.datn.teeshirt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "customer/index";
    }

    @GetMapping("/product")
    public String product() {
        return "customer/product/product-list";
    }

    @GetMapping("/detail")
    public String product_detail() {
        return "customer/product/product-detail";
    }

    @GetMapping("/cart")
    public String cart() {
        return "customer/account/shopping-cart";
    }

    @GetMapping("/login")
    public String login() {
        return "customer/account/login-register";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "customer/account/checkout";
    }

    @GetMapping("/about")
    public String about() {
        return "customer/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "customer/contact";
    }

    @GetMapping("/account")
    public String account() {
        return "customer/account/account";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin/index";
    }

    @GetMapping("/counter-sale")
    public String counter_sale() {
        return "admin/counter-sales";
    }

    @GetMapping("/admin/promotion-management")
    public String promotionManagement() {
        return "admin/promotion-management";
    }

    @GetMapping("/admin/refund-management")
    public String refundManagement() {
        return "admin/refund-management";
    }

    @GetMapping("/order")
    public String order() {
        return "/admin/order-management";
    }

    @GetMapping("/employee")
    public String employee() {
        return "/admin/employee-management";
    }
}
