package com.datn.teeshirt.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Repository.CustomerRepository;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerDTO> findAll() {
        return customerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findByEmailWithAddresses(String email) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // Force lazy loading of addresses
            customer.getAddresses().size();
        }
        return customerOpt;
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    // Tìm kiếm khách hàng theo tên hoặc SĐT
    public List<CustomerDTO> searchCustomers(String query) {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
            .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()))
            .map(this::toDTO) // Changed to toDTO as per original file
            .collect(Collectors.toList());
    }

    // Tạo khách hàng mới
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        //customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setAvatarUrl(customerDTO.getAvatarUrl());
        customer.setPassword(customerDTO.getPassword()); // Nên mã hóa nếu là đăng ký thực tế
        customer.setCreatedAt(java.time.LocalDateTime.now());
        customer.setUpdatedAt(java.time.LocalDateTime.now());
        Customer saved = customerRepository.save(customer);
        return toDTO(saved); // Changed to toDTO as per original file
    }

    // Chuyển từ Entity sang DTO
    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;
        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
				.phone(customer.getPhone())
                .avatarUrl(customer.getAvatarUrl())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    // Chuyển từ DTO sang Entity
    public Customer toEntity(CustomerDTO dto) {
        if (dto == null) return null;
        return Customer.builder()
                .customerId(dto.getCustomerId())
                .name(dto.getName())
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .build();
    }

    // Ví dụ hàm trả về CustomerDTO
    public CustomerDTO getCustomerDTOById(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return toDTO(customer);
    }

    // Tìm khách hàng theo ID
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }
}