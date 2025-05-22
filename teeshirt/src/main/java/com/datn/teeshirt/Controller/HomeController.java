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
}
