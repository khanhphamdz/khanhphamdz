package com.datn.teeshirt.Controller.Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.datn.teeshirt.Service.ProductService;

@Controller
public class HomeController {
    @Autowired
    ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("listLatestProducts", productService.getLatestProducts(8));
        model.addAttribute("listDiscountProducts", productService.getDiscountedProducts(8));
        model.addAttribute("listFeaturedProducts", productService.getLatestProducts(8)); // tạm thời dùng latest
        return "customer/index";
    }

    @GetMapping("/about")
    public String about() {
        return "customer/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "customer/contact";
    }

    @GetMapping("/shopping-cart")
    public String cart() {
        return "customer/account/shopping-cart";
    }
}
