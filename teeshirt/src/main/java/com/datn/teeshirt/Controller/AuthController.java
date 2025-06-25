package com.datn.teeshirt.Controller;

import com.datn.teeshirt.DTO.AuthRequestDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> register(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        CustomerDTO customerDTO = authService.register(authRequestDTO);
        return ResponseEntity.ok(customerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerDTO> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        CustomerDTO customerDTO = authService.login(authRequestDTO);
        return ResponseEntity.ok(customerDTO);
    }

}