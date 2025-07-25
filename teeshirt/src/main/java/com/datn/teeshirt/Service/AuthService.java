package com.datn.teeshirt.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.AuthRequestDTO;
import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.DTO.CustomerRegisterDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Repository.CustomerRepository;

@Service
public class AuthService {

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        EmailService emailService;

        @Autowired
        CartService cartService;

        @Autowired
        WishlistService wishlistService;

        public CustomerDTO register(CustomerRegisterDTO dto) {
                if (customerRepository.existsByEmail(dto.getEmail())) {
                        throw new RuntimeException("Email đã tồn tại");
                }
                Customer customer = Customer.builder()
                                .name(dto.getName())
                                .email(dto.getEmail())
                                .password(dto.getPassword())
                                .avatarUrl(null)
                                .build();
                Customer savedCustomer = customerRepository.save(customer);

                // Tạo Cart và Wishlist cho customer mới
                cartService.createCartForCustomer(savedCustomer);
                wishlistService.getOrCreateWishlist(savedCustomer);

                // gửi email cho khách hàng
                String subject = "Chào mừng bạn đến với TeeShirtVibe!";
                String text = "Xin chào " + savedCustomer.getName() + ",\n\n"
                                + "Bạn đã đăng ký tài khoản thành công tại TeeShirtVibe.\n"
                                + "Chúc bạn có trải nghiệm mua sắm vui vẻ!\n\n"
                                + "Trân trọng,\nTeeShirtVibe Team";
                try {
                        emailService.sendSimpleEmail(savedCustomer.getEmail(), subject, text);
                } catch (Exception e) {
                        e.printStackTrace();
                }

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