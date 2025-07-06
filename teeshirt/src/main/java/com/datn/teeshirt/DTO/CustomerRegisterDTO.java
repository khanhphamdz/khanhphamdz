package com.datn.teeshirt.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegisterDTO {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
} 