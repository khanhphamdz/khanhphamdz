package com.datn.teeshirt.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.ColorDTO;
import com.datn.teeshirt.Entity.Color;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Repository.ColorRepository;
import com.datn.teeshirt.Repository.ProductVariantRepository;

@Service
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Lấy tất cả màu sắc
    public List<ColorDTO> getAllColors() {
        return colorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tìm kiếm và phân trang
    public Page<ColorDTO> searchColors(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("colorId").descending());
        String searchTerm = search != null ? search.trim() : "";

        if (searchTerm.isEmpty()) {
            return colorRepository.findAll(pageable).map(this::convertToDTO);
        } else {
            return colorRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                    .map(this::convertToDTO);
        }
    }

    // Lấy màu sắc theo ID
    public Optional<ColorDTO> getColorById(Long id) {
        return colorRepository.findById(id).map(this::convertToDTO);
    }

    // 15. Lấy danh sách màu sắc theo product ID (chỉ những màu có variant)
    public List<ColorDTO> getColorsByProductId(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(productId);
        return variants.stream()
                .map(variant -> variant.getColor())
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm màu sắc mới
    public ColorDTO createColor(ColorDTO colorDTO) {
        // Kiểm tra trùng tên
        if (colorRepository.existsByNameIgnoreCase(colorDTO.getName())) {
            throw new RuntimeException("Tên màu sắc đã tồn tại");
        }

        // Kiểm tra trùng mã màu
        if (colorRepository.existsByHexCodeIgnoreCase(colorDTO.getHexCode())) {
            throw new RuntimeException("Mã màu đã tồn tại");
        }

        Color color = Color.builder()
                .name(colorDTO.getName().trim())
                .hexCode(colorDTO.getHexCode().toUpperCase())
                .build();

        Color savedColor = colorRepository.save(color);
        return convertToDTO(savedColor);
    }

    // Cập nhật màu sắc
    public ColorDTO updateColor(Long id, ColorDTO colorDTO) {
        Optional<Color> existingColor = colorRepository.findById(id);
        if (existingColor.isEmpty()) {
            throw new RuntimeException("Không tìm thấy màu sắc với ID: " + id);
        }

        Color color = existingColor.get();

        // Kiểm tra trùng tên (trừ chính nó)
        if (!color.getName().equalsIgnoreCase(colorDTO.getName().trim()) &&
                colorRepository.existsByNameIgnoreCase(colorDTO.getName().trim())) {
            throw new RuntimeException("Tên màu sắc đã tồn tại");
        }

        // Kiểm tra trùng mã màu (trừ chính nó)
        if (!color.getHexCode().equalsIgnoreCase(colorDTO.getHexCode()) &&
                colorRepository.existsByHexCodeIgnoreCase(colorDTO.getHexCode())) {
            throw new RuntimeException("Mã màu đã tồn tại");
        }

        color.setName(colorDTO.getName().trim());
        color.setHexCode(colorDTO.getHexCode().toUpperCase());
        Color updatedColor = colorRepository.save(color);
        return convertToDTO(updatedColor);
    }

    // Xóa màu sắc
    public boolean deleteColor(Long id) {
        Optional<Color> color = colorRepository.findById(id);
        if (color.isEmpty()) {
            return false;
        }

        // TODO: Kiểm tra xem có sản phẩm nào đang sử dụng màu sắc này không
        // Nếu có thì không cho xóa

        colorRepository.deleteById(id);
        return true;
    }

    // Chuyển đổi Entity sang DTO
    private ColorDTO convertToDTO(Color color) {
        return ColorDTO.builder()
                .colorId(color.getColorId())
                .name(color.getName())
                .hexCode(color.getHexCode())
                .createdAt(color.getCreatedAt())
                .build();
    }
}