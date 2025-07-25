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

    @GetMapping("/promotion")
    public String promotionManagementPage() {
        return "admin/product/promotion-management";
    }
<<<<<<< HEAD
    @GetMapping("/xxx")
    public String xxx() {
        return "admin/order/order-detail";
=======

    @GetMapping("/refund-management")
    public String refundManagementPage() {
        return "admin/product/refund-management";
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    }
}
