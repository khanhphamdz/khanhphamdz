package com.datn.teeshirt.Security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.Employee;
import com.datn.teeshirt.Service.CustomerService;
import com.datn.teeshirt.Service.EmployeeService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeService employeeService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm trong bảng Customer trước
        Optional<Customer> customerOpt = customerService.findByEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            return new User(
                    customer.getEmail(),
                    customer.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        }
        // Nếu không có, tìm trong bảng Employee
        Optional<Employee> employeeOpt = employeeService.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            String role = employee.getRole() ? "ROLE_ADMIN" : "ROLE_STAFF";
            return new User(
                    employee.getEmail(),
                    employee.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(role)));
        }
        throw new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
    }

    public Employee getEmployeeInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return employeeService.findByEmail(email).orElse(null);
    }

    public Customer getCustomerInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return customerService.findByEmail(email).orElse(null);
    }
}