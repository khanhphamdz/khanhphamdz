package com.datn.teeshirt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "/customer/index";
    }

    @GetMapping("/product")
    public String product() {
        return "/customer/product/product-list";
    }

    @GetMapping("/detail")
    public String product_detail() {
        return "/customer/product/product-detail";
    }

    @GetMapping("/cart")
    public String cart() {
        return "/customer/account/shopping-cart";
    }

    @GetMapping("/login")
    public String login() {
        return "/customer/account/login-register";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "/customer/account/checkout";
    }

    @GetMapping("/about")
    public String about() {
        return "/customer/about";
    }
}
