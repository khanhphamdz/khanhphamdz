package com.datn.teeshirt.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.CustomerDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Repository.CustomerRepository;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

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

    // Chuyển từ Entity sang DTO
    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;
        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
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
}