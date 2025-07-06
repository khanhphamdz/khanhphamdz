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

import com.datn.teeshirt.DTO.MaterialDTO;
import com.datn.teeshirt.Entity.Material;
import com.datn.teeshirt.Repository.MaterialRepository;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    // Lấy tất cả chất liệu
    public List<MaterialDTO> getAllMaterials() {
        return materialRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tìm kiếm và phân trang
    public Page<MaterialDTO> searchMaterials(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("materialId").descending());
        String searchTerm = search != null ? search.trim() : "";
        
        if (searchTerm.isEmpty()) {
            return materialRepository.findAll(pageable).map(this::convertToDTO);
        } else {
            return materialRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                    .map(this::convertToDTO);
        }
    }

    // Lấy chất liệu theo ID
    public Optional<MaterialDTO> getMaterialById(Long id) {
        return materialRepository.findById(id).map(this::convertToDTO);
    }

    // Thêm chất liệu mới
    public MaterialDTO createMaterial(MaterialDTO materialDTO) {
        // Kiểm tra trùng tên
        if (materialRepository.existsByNameIgnoreCase(materialDTO.getName())) {
            throw new RuntimeException("Tên chất liệu đã tồn tại");
        }

        Material material = Material.builder()
                .name(materialDTO.getName().trim())
                .build();

        Material savedMaterial = materialRepository.save(material);
        return convertToDTO(savedMaterial);
    }

    // Cập nhật chất liệu
    public MaterialDTO updateMaterial(Long id, MaterialDTO materialDTO) {
        Optional<Material> existingMaterial = materialRepository.findById(id);
        if (existingMaterial.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chất liệu với ID: " + id);
        }

        Material material = existingMaterial.get();
        
        // Kiểm tra trùng tên (trừ chính nó)
        if (!material.getName().equalsIgnoreCase(materialDTO.getName().trim()) &&
            materialRepository.existsByNameIgnoreCase(materialDTO.getName().trim())) {
            throw new RuntimeException("Tên chất liệu đã tồn tại");
        }

        material.setName(materialDTO.getName().trim());
        Material updatedMaterial = materialRepository.save(material);
        return convertToDTO(updatedMaterial);
    }

    // Xóa chất liệu
    public boolean deleteMaterial(Long id) {
        Optional<Material> material = materialRepository.findById(id);
        if (material.isEmpty()) {
            return false;
        }

        // TODO: Kiểm tra xem có sản phẩm nào đang sử dụng chất liệu này không
        // Nếu có thì không cho xóa
        
        materialRepository.deleteById(id);
        return true;
    }

    // Chuyển đổi Entity sang DTO
    private MaterialDTO convertToDTO(Material material) {
        return MaterialDTO.builder()
                .materialId(material.getMaterialId())
                .name(material.getName())
                .createdAt(material.getCreatedAt())
                .build();
    }
} 