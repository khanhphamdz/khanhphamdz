package com.datn.teeshirt.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {
    
    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "Truy cập bị từ chối");
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
        return "error/error";
    }
    
    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Trang không tồn tại");
        model.addAttribute("errorMessage", "Trang bạn đang tìm kiếm không tồn tại.");
        return "error/error";
    }
    
    @GetMapping("/500")
    public String serverError(Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Lỗi máy chủ");
        model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình xử lý yêu cầu.");
        return "error/error";
    }
}
