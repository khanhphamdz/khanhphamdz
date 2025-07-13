package com.datn.teeshirt.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminPage(Model model) {
        return "admin/index";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/account/danhNhap";
    }
    @GetMapping("/employees")
    public String employeesPage() {
        return "admin/account/employee-management";
    }
}
