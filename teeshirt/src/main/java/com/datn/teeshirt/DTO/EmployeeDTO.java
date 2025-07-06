package com.datn.teeshirt.DTO;

import java.time.LocalDate;

import com.datn.teeshirt.Entity.Employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Boolean gender;

    @Pattern(regexp = "d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    private LocalDate birthday;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private LocalDate hireDate;

    @NotNull(message = "Status is required")
    private String status;

    @Pattern(regexp = "\\d{12}", message = "Citizen ID must be 12 digits")
    private String citizenId;

    @NotNull(message = "Role is required")
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