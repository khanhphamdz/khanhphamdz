package com.datn.teeshirt.Controller;

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
        return "/admin/order/counter-sales";
    }

    @GetMapping("/admin/promotion-management")
    public String promotionManagement() {
        return "/admin/product/promotion-management";
    }

    @GetMapping("/admin/refund-management")
    public String refundManagement() {
        return "/admin/product/refund-management";
    }

    @GetMapping("/order")
    public String order() {
        return "/admin/order/order-management";
    }

    @GetMapping("/employee")
    public String employee() {
        return "/admin/account/employee-management";
    }

    @GetMapping("/product-management")
    public String productManagement() {
        return "/admin/product/product-management";
    }

    @GetMapping("/ass-product")
    public String add_product() {
        return "/admin/product/product-add-management";
    }

    @GetMapping("product-detail")
    public String detail_product() {
        return "/admin/product/product-detail-management";
    }

    @GetMapping("/customer-management")
    public String customerManagement() {
        return "/admin/account/customer-management";
    }

    @GetMapping("/admin/comment-manegement")
    public String commentManagement() {
        return "/admin/feedback-management";
    }

    @GetMapping("/admin/order-detail")
    public String order_detail() {
        return "/admin/order/order-detail";
    }
}
