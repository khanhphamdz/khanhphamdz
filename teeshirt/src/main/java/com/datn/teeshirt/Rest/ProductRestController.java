package com.datn.teeshirt.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Service.ProductService;

@RestController
@RequestMapping("/product/get-all")
public class ProductRestController {
    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(name = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(productService.findAllActive(page));
    }
}
