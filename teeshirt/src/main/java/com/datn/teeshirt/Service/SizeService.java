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

import com.datn.teeshirt.DTO.SizeDTO;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Entity.Size;
import com.datn.teeshirt.Repository.ProductVariantRepository;
import com.datn.teeshirt.Repository.SizeRepository;

@Service
public class SizeService {

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Lấy tất cả kích cỡ
    public List<SizeDTO> getAllSizes() {
        return sizeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tìm kiếm và phân trang
    public Page<SizeDTO> searchSizes(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sizeId").descending());
        String searchTerm = search != null ? search.trim() : "";

        if (searchTerm.isEmpty()) {
            return sizeRepository.findAll(pageable).map(this::convertToDTO);
        } else {
            return sizeRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                    .map(this::convertToDTO);
        }
    }

    // Lấy kích cỡ theo ID
    public Optional<SizeDTO> getSizeById(Long id) {
        return sizeRepository.findById(id).map(this::convertToDTO);
    }

    // 16. Lấy danh sách kích thước theo product ID (chỉ những size có variant)
    public List<SizeDTO> getSizesByProductId(Long productId) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(productId);
        return variants.stream()
                .map(variant -> variant.getSize())
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm kích cỡ mới
    public SizeDTO createSize(SizeDTO sizeDTO) {
        // Kiểm tra trùng tên
        if (sizeRepository.existsByNameIgnoreCase(sizeDTO.getName())) {
            throw new RuntimeException("Tên kích cỡ đã tồn tại");
        }

        Size size = Size.builder()
                .name(sizeDTO.getName().trim())
                .build();

        Size savedSize = sizeRepository.save(size);
        return convertToDTO(savedSize);
    }

    // Cập nhật kích cỡ
    public SizeDTO updateSize(Long id, SizeDTO sizeDTO) {
        Optional<Size> existingSize = sizeRepository.findById(id);
        if (existingSize.isEmpty()) {
            throw new RuntimeException("Không tìm thấy kích cỡ với ID: " + id);
        }

        Size size = existingSize.get();

        // Kiểm tra trùng tên (trừ chính nó)
        if (!size.getName().equalsIgnoreCase(sizeDTO.getName().trim()) &&
                sizeRepository.existsByNameIgnoreCase(sizeDTO.getName().trim())) {
            throw new RuntimeException("Tên kích cỡ đã tồn tại");
        }

        size.setName(sizeDTO.getName().trim());
        Size updatedSize = sizeRepository.save(size);
        return convertToDTO(updatedSize);
    }

    // Xóa kích cỡ
    public boolean deleteSize(Long id) {
        Optional<Size> size = sizeRepository.findById(id);
        if (size.isEmpty()) {
            return false;
        }

        // TODO: Kiểm tra xem có sản phẩm nào đang sử dụng kích cỡ này không
        // Nếu có thì không cho xóa

        sizeRepository.deleteById(id);
        return true;
    }

    // Chuyển đổi Entity sang DTO
    private SizeDTO convertToDTO(Size size) {
        return SizeDTO.builder()
                .sizeId(size.getSizeId())
                .name(size.getName())
                .createdAt(size.getCreatedAt())
                .build();
    }
}