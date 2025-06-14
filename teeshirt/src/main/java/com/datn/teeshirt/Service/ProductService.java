package com.datn.teeshirt.Service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.DTO.ProductAttributeDTO;
import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.DTO.AttributeDTO;
import com.datn.teeshirt.Entity.Category;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Repository.ProductRepository;

@Service
public class ProductService {
        @Autowired
        private ProductRepository repository;

        public ProductDTO convertToProductDTO(Product product) {
                ProductDTO dto = new ProductDTO();
                dto.setProductId(product.getProductId());
                dto.setName(product.getName());
                dto.setDescription(product.getDescription());
                dto.setShortDescription(product.getShortDescription());
                dto.setIsFeatured(product.getIsFeatured());
                dto.setStatus(product.getStatus());
                dto.setAttributes(
                                product.getProductAttributes().stream()
                                                .map(pa -> {
                                                        return ProductAttributeDTO.builder()
                                                                        .productAttributeId(pa.getProductAttributeId())
                                                                        .attributeName(pa.getAttribute().getName())
                                                                        .attributeValue(pa.getTerm().getName())
                                                                        .build();
                                                })
                                                .collect(Collectors.toList()));
                dto.setCategories(
                                product.getProductCategories().stream()
                                                .map(pc -> {
                                                        Category category = pc.getCategory();
                                                        return CategoryDTO.builder()
                                                                        .categoryId(category.getCategoryId())
                                                                        .name(category.getName())
                                                                        .description(category.getDescription())
                                                                        .build();
                                                })
                                                .collect(Collectors.toList()));
                dto.setVariants(
                                product.getVariants().stream()
                                                .map(variant -> {
                                                        return ProductVariantDTO.builder()
                                                                        .variantId(variant.getVariantId())
                                                                        .productId(variant.getProduct().getProductId())
                                                                        .sku(variant.getSku())
                                                                        .barcode(variant.getBarcode())
                                                                        .price(variant.getPrice())
                                                                        .discountPrice(variant.getDiscountPrice())
                                                                        .discountPriceStartAt(variant
                                                                                        .getDiscountPriceStartAt())
                                                                        .discountPriceEndAt(
                                                                                        variant.getDiscountPriceEndAt())
                                                                        .attributes(variant.getVariantAttributes()
                                                                                        .stream()
                                                                                        .map(va -> {
                                                                                                return AttributeDTO
                                                                                                                .builder()
                                                                                                                .attributeId(va.getVariantAttributeId())
                                                                                                                .attributeName(va
                                                                                                                                .getAttribute()
                                                                                                                                .getName())
                                                                                                                .attributeValue(va
                                                                                                                                .getTerm()
                                                                                                                                .getName())
                                                                                                                .build();
                                                                                        })
                                                                                        .collect(Collectors.toList()))
                                                                        .images(variant.getImages().stream()
                                                                                        .map(vi -> {
                                                                                                return ProductImageDTO
                                                                                                                .builder()
                                                                                                                .imageId(vi.getImageId())
                                                                                                                .imageUrl(vi.getImageUrl())
                                                                                                                .image_type(String
                                                                                                                                .valueOf(vi.getImageType()))
                                                                                                                .build();
                                                                                        })
                                                                                        .collect(Collectors.toList()))
                                                                        .build();
                                                })
                                                .collect(Collectors.toList()));
                dto.setCreatedAt(product.getCreatedAt());
                dto.setUpdatedAt(product.getUpdatedAt());
                return dto;
        }

        public Page<ProductDTO> findAllActive(int page) {
                Pageable pageable = PageRequest.of(page, 10);
                Page<Product> page_product = repository.findAllActive(pageable);
                return page_product.map(this::convertToProductDTO);
        }
}
