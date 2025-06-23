package com.datn.teeshirt.Rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.Service.AttributeService;
import com.datn.teeshirt.Service.AttributeTermService;
import com.datn.teeshirt.Service.CategoryService;
import com.datn.teeshirt.Service.CloudinaryService;
import com.datn.teeshirt.Service.ProductService;
import com.datn.teeshirt.Service.ProductVariantService;

@RestController
@RequestMapping("/api/product")
public class ProductRestController {
    @Autowired
    ProductService productService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private AttributeTermService attributeTermService;

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(name = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(productService.findAllActive(page));
    }

    @GetMapping("/get-latest")
    public ResponseEntity<?> getLatestProducts() {
        return ResponseEntity.ok(productService.findLatestProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<Map<String, Object>> searchProduct(
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        Map<String, Object> response = new HashMap<>();
        System.out.println(keyword);
        try {
            List<ProductDTO> listProduct = productService.searchProduct(keyword);
            response.put("status", true);
            response.put("listProduct", listProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file, null);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Upload thất bại: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.createProduct(productDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesTree());
    }

    @GetMapping("/attributes")
    public ResponseEntity<?> getAllAttributes() {
        return ResponseEntity.ok(attributeService.getAllAttributes());
    }

    @GetMapping("/attribute-terms")
    public ResponseEntity<?> getAllAttributeTerms() {
        return ResponseEntity.ok(attributeTermService.getAllAttributeTerms());
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<?> getProductVariants(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getVariantsByProductId(id));
    }

    @GetMapping("/attribute-terms/by-attribute/{attributeId}")
    public ResponseEntity<?> getTermsByAttribute(@PathVariable Long attributeId) {
        return ResponseEntity.ok(attributeTermService.getTermsByAttribute(attributeId));
    }

    @PostMapping("/attribute-terms")
    public ResponseEntity<?> addAttributeTerm(@RequestBody com.datn.teeshirt.DTO.AttributeTermsDTO dto) {
        return ResponseEntity.ok(attributeTermService.addAttributeTerm(dto));
    }
}
