package com.datn.teeshirt.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.datn.teeshirt.Security.CustomUserDetailsService;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    CustomUserDetailsService userDetailsService;

    @ModelAttribute
    public void addUserInfoToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Nếu chưa đăng nhập hoặc là anonymous, bỏ qua
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return;
        }

        // Kiểm tra nếu user có role CUSTOMER thì bỏ qua logic phía dưới
        boolean isCustomer = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CUSTOMER"));
        if (!isCustomer) {
            model.addAttribute("userName", userDetailsService.getEmployeeInfo().getFullName());
            model.addAttribute("userRole", userDetailsService.getEmployeeInfo().getRole() ? "Quản Lý" : "Nhân Viên");
        }
    }
}
