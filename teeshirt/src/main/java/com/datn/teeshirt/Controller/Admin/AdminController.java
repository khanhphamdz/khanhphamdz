package com.datn.teeshirt.Controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.teeshirt.Security.CustomUserDetailsService;
import com.datn.teeshirt.Service.ProductService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    ProductService productService;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @GetMapping
    public String adminPage(Model model) {
        return "admin/index";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/account/danhNhap";
    }
}
