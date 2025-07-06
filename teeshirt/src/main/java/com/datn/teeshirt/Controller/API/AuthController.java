package com.datn.teeshirt.Controller.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.AuthRequestDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.DTO.CustomerRegisterDTO;
import com.datn.teeshirt.DTO.ForgotPasswrdRequestDTO;
import com.datn.teeshirt.Service.AuthService;
import com.datn.teeshirt.Service.EmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    EmailService forgotPasswordService;

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@RequestBody CustomerRegisterDTO registerDTO) {
        try {
            CustomerDTO customerDTO = authService.register(registerDTO);
            return ResponseEntity.ok(new ResponseObject("ok", "Đăng ký thành công!", customerDTO));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Đăng ký thất bại!", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDTO> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        CustomerDTO customerDTO = authService.login(authRequestDTO);
        return ResponseEntity.ok(customerDTO);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseObject> getPassword(@RequestBody ForgotPasswrdRequestDTO request) {
        Boolean sendEmail = forgotPasswordService.sendPasswordToEmail(request.getEmail());
        if (sendEmail) {
            return ResponseEntity.ok(new ResponseObject("ok", "Đã gửi mật khẩu, vui lòng kiểm tra email", true));
        }
        return ResponseEntity.ok(new ResponseObject("false", "Gửi mật khẩu thất bại, vui lòng thử lại sau", false));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(new LoginStatusResponse(false, null, null));
        }
        // Trả về trạng thái đăng nhập và thông tin user
        return ResponseEntity.ok(new LoginStatusResponse(true, auth.getName(), auth.getAuthorities()));
    }

    // Inner class trả về trạng thái đăng nhập và thông tin user
    public static class LoginStatusResponse {
        public boolean isLoggedIn;
        public String username;
        public Object roles;

        public LoginStatusResponse(boolean isLoggedIn, String username, Object roles) {
            this.isLoggedIn = isLoggedIn;
            this.username = username;
            this.roles = roles;
        }
    }
}