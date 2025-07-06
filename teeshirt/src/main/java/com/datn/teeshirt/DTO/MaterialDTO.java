package com.datn.teeshirt.DTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {
    private Long materialId;

    @NotBlank(message = "Tên chất liệu không được để trống")
    @Size(max = 50, message = "Tên chất liệu tối đa 50 ký tự")
    private String name;

    private LocalDateTime createdAt;
} 