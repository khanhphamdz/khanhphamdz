package com.datn.teeshirt.Controller.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.datn.teeshirt.Service.EmailService;

@RestController
@RequestMapping("/api/contact")
public class ContactAPI {
    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendContact(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String message = body.get("message");
        if (name == null || name.isBlank() || email == null || email.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Vui lòng nhập đầy đủ thông tin."
            ));
        }
        try {
            String subject = "[Liên hệ từ website TeeShirtVibe]";
            String text = "Bạn nhận được tin nhắn liên hệ từ website:\n" +
                "Họ tên: " + name + "\n" +
                "Email: " + email + "\n" +
                "Nội dung: " + message;
            emailService.sendSimpleEmail("khanhpqpt00043@gmail.com", subject, text);
            return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Gửi tin nhắn thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Gửi tin nhắn thất bại. Vui lòng thử lại sau."
            ));
        }
    }
} 