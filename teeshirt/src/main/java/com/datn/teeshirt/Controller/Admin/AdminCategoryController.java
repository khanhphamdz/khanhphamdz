package com.datn.teeshirt.Controller.Admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.Service.CategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/product/category")
public class AdminCategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        Page<CategoryDTO> categories = categoryService.searchCategories(search, page, 10);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("totalItems", categories.getTotalElements());
        
        return "admin/product/category-management";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute CategoryDTO categoryDTO, RedirectAttributes redirectAttributes) {
        try {
            categoryService.addCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/product/category";
    }

    @PostMapping("/edit")
    public String editCategory(@ModelAttribute CategoryDTO categoryDTO, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/product/category";
    }

    @PostMapping("/delete")
    public String deleteCategory(@RequestParam Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = categoryService.deleteCategory(categoryId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục để xóa!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/product/category";
    }

    // ========== API ENDPOINTS ==========

    @PostMapping("/api/add")
    @ResponseBody
    public Map<String, Object> addCategoryAPI(@Valid CategoryDTO categoryDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        
        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            categoryService.addCategory(categoryDTO);
            response.put("success", true);
            response.put("message", "Thêm danh mục thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/api/update/{id}")
    @ResponseBody
    public Map<String, Object> updateCategoryAPI(@PathVariable Long id, @Valid CategoryDTO categoryDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        
        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            categoryDTO.setCategoryId(id);
            categoryService.updateCategory(categoryDTO);
            response.put("success", true);
            response.put("message", "Cập nhật danh mục thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/api/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategoryAPI(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa danh mục thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy danh mục để xóa");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}