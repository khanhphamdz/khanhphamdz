package com.datn.teeshirt.Controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.datn.teeshirt.Service.OrderService;


@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    OrderService orderService;
    @GetMapping
    public String adminOrderPage() {
        return "admin/order/order-management";
    }
    @GetMapping("/{orderId}")
    public String adminOrdersPage(@PathVariable String orderId, Model model) {
        System.out.println(orderId);
        model.addAttribute("orderId", orderId);
        return "admin/order/order-detail";
    }

    @GetMapping("/counter-sale")
    public String counterSalePage() {
        return "admin/order/counter-sale";
    }
}
