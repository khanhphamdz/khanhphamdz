package com.datn.teeshirt.Controller.Client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.Service.CategoryService;
import com.datn.teeshirt.Service.ColorService;
import com.datn.teeshirt.Service.ProductService;
import com.datn.teeshirt.Service.SizeService;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ColorService colorService;

    @Autowired
    SizeService sizeService;

    // Route danh sách sản phẩm (đã dynamic + phân trang)
    @GetMapping
    public String product(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Page<ProductDTO> products = productService.getProducts(page, size, null, null, null, null, true);
            model.addAttribute("products", products);
            // Lấy dữ liệu cho bộ lọc
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("colors", colorService.getAllColors());
            model.addAttribute("sizes", sizeService.getAllSizes());
        } catch (Exception e) {
            // Nếu có lỗi, tạo danh sách rỗng
            model.addAttribute("products", Page.empty());
            model.addAttribute("categories", java.util.List.of());
            model.addAttribute("colors", java.util.List.of());
            model.addAttribute("sizes", java.util.List.of());
        }
        return "customer/product/product-list";
    }

    @GetMapping("/detail/{id}")
    public String product(@PathVariable Long id, Model model) {
        try {
            ProductDTO product = productService.getProductById(id);
            model.addAttribute("product", product);

            // Truyền variants cho trang chi tiết - dùng method có sẵn
            model.addAttribute("variants", productService.getVariantsByProductId(id));

            // CHỈ lấy màu/size có variant thực tế của sản phẩm này
            model.addAttribute("availableSizes", sizeService.getSizesByProductId(id));
            model.addAttribute("availableColors", colorService.getColorsByProductId(id));

            // Lấy sản phẩm liên quan (4 sản phẩm)
            model.addAttribute("relatedProducts", productService.getRelatedProducts(id, 4));

        } catch (Exception e) {
            // Nếu không tìm thấy sản phẩm, redirect về trang danh sách
            return "redirect:/product/list-products";
        }
        return "customer/product/product-detail";
    }

    // Route filter sản phẩm với đầy đủ tính năng
    @GetMapping("/filter")
    public String filterProducts(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            // Lấy sản phẩm với filter đầy đủ
            Page<ProductDTO> products = productService.getProducts(
                    page, size, keyword, categoryId, colorId, sizeId, minPrice, maxPrice, true);
            model.addAttribute("products", products);

            // Lấy dữ liệu cho bộ lọc
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("colors", colorService.getAllColors());
            model.addAttribute("sizes", sizeService.getAllSizes());

            // Giữ trạng thái filter hiện tại
            model.addAttribute("currentKeyword", keyword);
            model.addAttribute("currentCategoryId", categoryId);
            model.addAttribute("currentColorId", colorId);
            model.addAttribute("currentSizeId", sizeId);
            model.addAttribute("currentMinPrice", minPrice);
            model.addAttribute("currentMaxPrice", maxPrice);

        } catch (Exception e) {
            model.addAttribute("products", Page.empty());
            model.addAttribute("categories", java.util.List.of());
            model.addAttribute("colors", java.util.List.of());
            model.addAttribute("sizes", java.util.List.of());
        }

        // Kiểm tra nếu là AJAX request thì trả về fragment
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/product/product-list :: product-content";
        }

        return "customer/product/product-list";
    }

    @GetMapping("/product/detail/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        // Gộp tất cả ảnh sản phẩm và ảnh các biến thể
        List<ProductImageDTO> allImages = new ArrayList<>();
        if (product.getImages() != null)
            allImages.addAll(product.getImages());
        if (product.getVariants() != null) {
            for (ProductVariantDTO variant : product.getVariants()) {
                if (variant.getImages() != null)
                    allImages.addAll(variant.getImages());
            }
        }
        model.addAttribute("product", product);
        model.addAttribute("allImages", allImages);
        // Đánh giá trung bình
        model.addAttribute("averageRating", product.getAverageRating());
        // Sản phẩm liên quan
        List<ProductDTO> relatedProducts = productService.getRelatedProducts(id, 8);
        model.addAttribute("relatedProducts", relatedProducts);
        return "customer/product/product-detail";
    }
}
