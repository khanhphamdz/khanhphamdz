package com.datn.teeshirt.Service;

import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.DTO.AuthRequestDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public CustomerDTO register(AuthRequestDTO authRequestDTO) {
        if (customerRepository.existsByEmail(authRequestDTO.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (!authRequestDTO.getPassword().equals(authRequestDTO.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        Customer customer = Customer.builder()
                .name(authRequestDTO.getName())
                .email(authRequestDTO.getEmail())
                .password(passwordEncoder.encode(authRequestDTO.getPassword()))
                .avatarUrl(null) // hoặc set mặc định nếu muốn
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        return CustomerDTO.builder()
                .customerId(savedCustomer.getCustomerId())
                .name(savedCustomer.getName())
                .email(savedCustomer.getEmail())
                .avatarUrl(savedCustomer.getAvatarUrl())
                .createdAt(savedCustomer.getCreatedAt())
                .updatedAt(savedCustomer.getUpdatedAt())
                .build();
    }

    public CustomerDTO login(AuthRequestDTO authRequestDTO) {
        Customer customer = customerRepository.findByEmail(authRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));
        if (!passwordEncoder.matches(authRequestDTO.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
                .avatarUrl(customer.getAvatarUrl())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}