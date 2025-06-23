package com.datn.teeshirt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {
    @GetMapping("/customer/account/account")
    public String accountPage() {
        return "customer/account/account";
    }
}
