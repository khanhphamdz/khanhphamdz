package com.datn.teeshirt.DTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
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
public class ColorDTO {
    private Long colorId;

    @NotBlank(message = "Tên màu sắc không được để trống")
    @Size(max = 50, message = "Tên màu sắc tối đa 50 ký tự")
    private String name;

    @NotBlank(message = "Mã màu không được để trống")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu phải có định dạng #RRGGBB")
    @Size(min = 7, max = 7, message = "Mã màu phải có 7 ký tự")
    private String hexCode;

    private LocalDateTime createdAt;
} 