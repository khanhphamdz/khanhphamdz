package com.datn.teeshirt.Controller.API;

import com.datn.teeshirt.Service.CategoryService;
import com.datn.teeshirt.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter-sale/products")
public class CounterSaleProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    // Lấy danh sách sản phẩm (tìm kiếm, lọc, phân trang)
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProducts(page, size, keyword, categoryId));
    }

    // Lấy chi tiết sản phẩm (bao gồm biến thể)
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Lấy sản phẩm theo mã vạch (barcode)
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<?> getProductByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getProductByBarcode(barcode));
    }

    // Lấy danh sách danh mục sản phẩm
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}