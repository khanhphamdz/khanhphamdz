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

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;
    
    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gender;
    
    @Pattern(regexp = "\\d{10}", message = "Số điện thoại phải gồm đúng 10 chữ số")
    private String phoneNumber;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 3, max = 20, message = "Mật khẩu phải từ 3 đến 20 ký tự")
    private String password;
    
    private LocalDate birthday;
    
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
    
    private LocalDate hireDate;
    
    @NotNull(message = "Vui lòng chọn trạng thái làm việc")
    private String status;
    
    @Pattern(regexp = "\\d{12}", message = "CCCD phải gồm đúng 12 chữ số")
    private String citizenId;
    
    @NotNull(message = "Vui lòng chọn phân quyền")
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