package com.datn.teeshirt.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {
    @GetMapping
    public String adminOrderPage() {
        return "admin/order/order-management";
    }

    @GetMapping("/counter-sale")
    public String counterSalePage() {
        return "admin/order/counter-sale";
    }
}
