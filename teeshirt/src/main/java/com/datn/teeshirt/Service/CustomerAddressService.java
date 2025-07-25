package com.datn.teeshirt.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.CustomerAddressDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.CustomerAddress;
import com.datn.teeshirt.Repository.CustomerAddressRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomerAddressService {

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    public List<CustomerAddress> findByCustomerId(Long customerId) {
        return customerAddressRepository.findByCustomerCustomerId(customerId);
    }

    public CustomerAddress save(CustomerAddress address) {
        return customerAddressRepository.save(address);
    }

    public CustomerAddress addAddress(Customer customer, String name, String phone, String provinceId,
            String districtId, String wardId, String specificAddress) {
        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .name(name)
                .phone(phone)
                .provinceId(provinceId)
                .districtId(districtId)
                .wardId(wardId)
                .specificAddress(specificAddress)
                .build();
        return customerAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        customerAddressRepository.deleteByAddressId(addressId);
    }

    public CustomerAddress findById(Long addressId) {
        return customerAddressRepository.findById(addressId).orElse(null);
    }

    // Chuyển từ Entity sang DTO
    public CustomerAddressDTO toDTO(CustomerAddress address) {
        if (address == null) return null;
        return CustomerAddressDTO.builder()
                .addressId(address.getAddressId())
                .customerId(address.getCustomer() != null ? address.getCustomer().getCustomerId() : null)
                .provinceId(address.getProvinceId())
                .districtId(address.getDistrictId())
                .wardId(address.getWardId())
                .specificAddress(address.getSpecificAddress())
                .phone(address.getPhone())
                .name(address.getName())
                .createdAt(address.getCreatedAt())
                .build();
    }

    // Chuyển từ DTO sang Entity
    public CustomerAddress toEntity(CustomerAddressDTO dto, Customer customer) {
        if (dto == null) return null;
        return CustomerAddress.builder()
                .addressId(dto.getAddressId())
                .customer(customer)
                .provinceId(dto.getProvinceId())
                .districtId(dto.getDistrictId())
                .wardId(dto.getWardId())
                .specificAddress(dto.getSpecificAddress())
                .phone(dto.getPhone())
                .name(dto.getName())
                .build();
    }

    // Ví dụ hàm trả về danh sách CustomerAddressDTO theo customerId
    public List<CustomerAddressDTO> getAddressDTOsByCustomerId(Long customerId) {
        List<CustomerAddress> addresses = customerAddressRepository.findByCustomerCustomerId(customerId);
        return addresses.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void setDefaultAddress(Long customerId, Long addressId) {
        // Unset previous default addresses
        List<CustomerAddress> addresses = findByCustomerId(customerId);
        for (CustomerAddress address : addresses) {
            if (address.getIsDefault() != null && address.getIsDefault()) {
                address.setIsDefault(false);
                save(address);
            }
        }
        // Set the new default address
        CustomerAddress newDefault = findById(addressId);
        if (newDefault != null && newDefault.getCustomer().getCustomerId().equals(customerId)) {
            newDefault.setIsDefault(true);
            save(newDefault);
        }
    }
}
