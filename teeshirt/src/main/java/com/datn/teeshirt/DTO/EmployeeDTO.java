package com.datn.teeshirt.DTO;

import java.time.LocalDate;

import com.datn.teeshirt.Entity.Employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long employeeId;

    private String fullName;

    private Boolean gender;

    private String phoneNumber;

    private String email;

    private String password;

    private LocalDate birthday;

    private String address;

    private LocalDate hireDate;

    private String status;

    private String citizenId;

    private Boolean role;

    // Convert DTO to Entity
    public Employee toEntity() {
        return Employee.builder()
                .employeeId(this.employeeId)
                .fullName(this.fullName)
                .gender(this.gender)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .password(this.password)
                .birthday(this.birthday)
                .address(this.address)
                .hireDate(this.hireDate)
                .status(this.status)
                .citizenId(this.citizenId)
                .role(this.role)
                .build();
    }

    // Convert Entity to DTO
    public static EmployeeDTO fromEntity(Employee employee) {
        return EmployeeDTO.builder()
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .gender(employee.getGender())
                .phoneNumber(employee.getPhoneNumber())
                .email(employee.getEmail())
                .password(employee.getPassword())
                .birthday(employee.getBirthday())
                .address(employee.getAddress())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .citizenId(employee.getCitizenId())
                .role(employee.getRole())
                .build();
    }
}