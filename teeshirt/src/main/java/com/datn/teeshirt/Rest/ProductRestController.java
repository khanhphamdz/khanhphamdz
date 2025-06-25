package com.datn.teeshirt.Rest;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.Service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductRestController {
    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(value = "page", defaultValue = "0") int page) {
        Page<ProductDTO> products = productService.findAll(page);
        if (products == null || products.isEmpty()) {
            return ResponseEntity.ok("No products found");
        }
        return ResponseEntity.ok(products);
    }
}
