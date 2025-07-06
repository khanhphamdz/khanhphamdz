package com.datn.teeshirt.Controller.Admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.Service.ProductService;

@Controller
@RequestMapping("/admin/product")
public class AdminProductController {
    @Autowired
    ProductService productService;

    @GetMapping
    public String getAllProducts(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        Boolean statusValue = null;
        if ("in-stock".equals(status))
            statusValue = true;
        if ("out-of-stock".equals(status))
            statusValue = false;

        Page<ProductDTO> products = productService.filterProducts(categoryId, statusValue, keyword, sort, page, size);
        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("categories", productService.getAllCategoryDTOs());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("keyword", keyword);
        return "admin/product/product-management";
    }

    // @GetMapping("/{id}")
    // public String getProductDetail(@RequestParam("id") Long id, Model model) {
    // ProductDTO product = productService.getProductById(id);
    // if (product == null) {
    // model.addAttribute("error", "Product not found");
    // return "admin/error";
    // }
    // model.addAttribute("product", product);
    // return "admin/product/product-detail";
    // }

    @GetMapping("/detail/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/product-management";
        }
        List<CategoryDTO> hierarchicalCategories = productService.getAllCategoryDTOs();
        List<Long> allCategoryIds = productService.getAllFlatCategoryIds();
        List<Long> productCategoryIds = productService.getProductCategoryIds(id);

        model.addAttribute("product", product);
        model.addAttribute("categories", hierarchicalCategories);
        model.addAttribute("allCategoryIds", allCategoryIds);
        model.addAttribute("productCategoryIds", productCategoryIds);
        model.addAttribute("materials", productService.getAllMaterials());
        return "admin/product/product-detail-management";
    }

    @PostMapping("/update")
    public String updateProduct(@ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "selectedCategories", required = false) List<Long> selectedCategoryIds,
            RedirectAttributes redirectAttributes) {
        try {
            // Cập nhật danh mục sản phẩm
            if (selectedCategoryIds != null && !selectedCategoryIds.isEmpty()) {
                productService.updateProductCategories(productDTO.getProductId(), selectedCategoryIds);
            } else {
                // Nếu không có danh mục nào được chọn, xóa tất cả danh mục hiện tại
                productService.updateProductCategories(productDTO.getProductId(), new ArrayList<>());
            }

            productService.updateProduct(productDTO.getProductId(), productDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/product/detail/" + productDTO.getProductId();
    }

    @PostMapping("/variant/edit")
    public String editVariant(@ModelAttribute ProductVariantDTO variantDTO,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateVariant(variantDTO.getVariantId(), variantDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật biến thể thất bại: " + e.getMessage());
        }
        return "redirect:/admin/product/detail/" + variantDTO.getProductId();
    }

    @PostMapping("/variant/delete")
    public String deleteVariant(@RequestParam Long variantId,
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes) {
        try {
            productService.deleteVariant(variantId);
            redirectAttributes.addFlashAttribute("success", "Xóa biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa biến thể thất bại: " + e.getMessage());
        }
        return "redirect:/admin/product/detail/" + productId;
    }

    @GetMapping("/add-product")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", productService.getAllCategoryDTOs());
        model.addAttribute("materials", productService.getAllMaterials());
        return "admin/product/product-add-management";
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "selectedCategories", required = false) List<Long> selectedCategoryIds,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            RedirectAttributes redirectAttributes) {
        try {
            // 1. Lưu sản phẩm
            ProductDTO savedProduct = productService.createProduct(productDTO);
            // 2. Lưu danh mục
            if (selectedCategoryIds != null && !selectedCategoryIds.isEmpty()) {
                productService.updateProductCategories(savedProduct.getProductId(), selectedCategoryIds);
            }
            // 3. Upload ảnh thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
                productService.uploadProductImages(savedProduct.getProductId(), new MultipartFile[] { thumbnail });
            }
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            return "redirect:/admin/product/detail/" + savedProduct.getProductId();
        } catch (Exception e) {
            // Log chi tiết lỗi
            System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/product/add-product";
        }
    }
}
