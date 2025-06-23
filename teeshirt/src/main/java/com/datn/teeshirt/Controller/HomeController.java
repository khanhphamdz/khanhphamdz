package com.datn.teeshirt.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.Service.ProductService;

@Controller
public class HomeController {
    @Autowired
    ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        Page<ProductDTO> listLatestProducts = productService.findLatestProducts();
        model.addAttribute("listLatestProducts", listLatestProducts);
        return "customer/index";
    }

    @GetMapping("/product/detail/{id}")
    public String product(@PathVariable Long id, Model model) {
        ProductDTO productDTO = productService.findById(id);
        model.addAttribute("product", productDTO);
        return "customer/product/product-detail";
    }   
    @GetMapping("/product/list-products")
    public String product() {
        return "customer/product/product-list";
    }

    @GetMapping("/about")
    public String about() {
        return "customer/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "customer/contact";
    }
}
