package com.datn.teeshirt.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.Entity.Category;
import com.datn.teeshirt.Repository.CategoryRepository;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> result = new ArrayList<>();
        for (Category cat : categories) {
            result.add(convertToDTO(cat));
        }
        return result;
    }

    // Tìm kiếm và phân trang
    public Page<CategoryDTO> searchCategories(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("categoryId").descending());
        String searchTerm = search != null ? search.trim() : "";
        
        if (searchTerm.isEmpty()) {
            return categoryRepository.findAll(pageable).map(this::convertToDTO);
        } else {
            return categoryRepository.findByNameContainingIgnoreCase(searchTerm, pageable)
                    .map(this::convertToDTO);
        }
    }

    // Lấy danh mục theo ID
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::convertToDTO);
    }

    public void addCategory(CategoryDTO dto) {
        // Kiểm tra trùng tên
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        Category cat = new Category();
        cat.setName(dto.getName().trim());
        if (dto.getParentId() != null) {
            cat.setParent(categoryRepository.findById(dto.getParentId()).orElse(null));
        }
        categoryRepository.save(cat);
    }

    public void updateCategory(CategoryDTO dto) {
        Category cat = categoryRepository.findById(dto.getCategoryId()).orElseThrow();
        
        // Kiểm tra trùng tên (trừ chính nó)
        if (!cat.getName().equalsIgnoreCase(dto.getName().trim()) &&
            categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        cat.setName(dto.getName().trim());
        if (dto.getParentId() != null) {
            cat.setParent(categoryRepository.findById(dto.getParentId()).orElse(null));
        } else {
            cat.setParent(null);
        }
        categoryRepository.save(cat);
    }

    public boolean deleteCategory(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            return false;
        }

        // TODO: Kiểm tra xem có sản phẩm nào đang sử dụng danh mục này không
        // Nếu có thì không cho xóa
        
        categoryRepository.deleteById(id);
        return true;
    }

    public String getNameById(Long id) {
        return categoryRepository.findById(id).map(Category::getName).orElse("");
    }

    // Chuyển đổi Entity sang DTO
    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : "")
                .build();
    }
} 