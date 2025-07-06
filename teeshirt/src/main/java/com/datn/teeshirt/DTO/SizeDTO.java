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
public class SizeDTO {
    private Long sizeId;

    @NotBlank(message = "Tên kích cỡ không được để trống")
    @Size(max = 10, message = "Tên kích cỡ tối đa 10 ký tự")
    private String name;

    private LocalDateTime createdAt;
} 