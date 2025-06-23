package com.datn.teeshirt.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.teeshirt.DTO.AttributeDTO;
import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
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
                                                                        .build();
                                                })
                                                .collect(Collectors.toList()));
                dto.setImages(product.getImages().stream()
                                .map(pi -> {
                                        return ProductImageDTO
                                                        .builder()
                                                        .imageId(pi.getImageId())
                                                        .productId(pi.getProduct() != null
                                                                        && pi.getProduct().getProductId() != null
                                                                                        ? String.valueOf(pi.getProduct()
                                                                                                        .getProductId())
                                                                                        : "No")
                                                        .variantId(pi.getVariant() != null
                                                                        && pi.getVariant().getVariantId() != null
                                                                                        ? String.valueOf(pi.getVariant()
                                                                                                        .getVariantId())
                                                                                        : "No")
                                                        .imageUrl(pi.getImageUrl())
                                                        .image_type(String
                                                                        .valueOf(pi.getImageType()))
                                                        .build();
                                })
                                .collect(Collectors.toList()));
                dto.setAttributes(
                                product.getProductAttributes().stream()
                                                .collect(Collectors.groupingBy(pa -> pa.getAttribute().getName()))
                                                .entrySet().stream()
                                                .map(entry -> {
                                                        // Mỗi entry là 1 thuộc tính, value là list ProductAttribute (có
                                                        // thể nhiều giá trị)
                                                        var first = entry.getValue().get(0);
                                                        return com.datn.teeshirt.DTO.ProductAttributeDTO.builder()
                                                                        .productAttributeId(
                                                                                        first.getProductAttributeId())
                                                                        .attributeName(entry.getKey())
                                                                        .attributeValue(
                                                                                        entry.getValue().stream()
                                                                                                        .filter(pa -> pa.getTerm() != null)
                                                                                                        .map(pa -> com.datn.teeshirt.DTO.AttributeTermsDTO
                                                                                                                        .builder()
                                                                                                                        .termId(pa.getTerm()
                                                                                                                                        .getTermId())
                                                                                                                        .attributeId(pa.getAttribute()
                                                                                                                                        .getAttributeId())
                                                                                                                        .term(pa.getTerm()
                                                                                                                                        .getName())
                                                                                                                        .build())
                                                                                                        .collect(Collectors
                                                                                                                        .toList()))
                                                                        .isVariation(first.getIsVariation())
                                                                        .build();
                                                })
                                                .collect(Collectors.toList()));
                return dto;
        }

        public Page<ProductDTO> findAllActive(int page) {
                Pageable pageable = PageRequest.of(page, 10);
                Page<Product> page_product = repository.findAllActive(pageable);
                return page_product.map(this::convertToProductDTO);
        }

        public Page<ProductDTO> findLatestProducts() {
                Pageable pageable = PageRequest.of(0, 8);
                Page<Product> page_product = repository.findLatestProducts(pageable);
                return page_product.map(this::convertToProductDTO);
        }

        public ProductDTO findById(Long id) {
                Product product = repository.findById(id).get();
                return convertToProductDTO(product);
        }

        public List<ProductDTO> searchProduct(String keyword) {
                System.out.println("Searching for keyword: " + keyword);
                List<Product> listProduct = repository.search(keyword);
                System.out.println("Found " + listProduct.size() + " products from repository");
                List<ProductDTO> result = listProduct.stream()
                                .map(product -> {
                                        System.out.println("Converting product: " + product.getName());
                                        return convertToProductDTO(product);
                                })
                                .collect(Collectors.toList());
                System.out.println("Final result size: " + result.size());
                return result;
        }

        @Transactional
        public ProductDTO createProduct(ProductDTO productDTO) {
                Product product = new Product();
                product.setName(productDTO.getName());
                product.setDescription(productDTO.getDescription());
                product.setShortDescription(productDTO.getShortDescription());
                product.setIsFeatured(productDTO.getIsFeatured());
                product.setStatus(productDTO.getStatus());
                // TODO: Gán danh mục, thuộc tính, ảnh, variants nếu cần
                Product saved = repository.save(product);
                return convertToProductDTO(saved);
        }

        @Transactional
        public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
                Product product = repository.findById(id).orElseThrow();
                product.setName(productDTO.getName());
                product.setDescription(productDTO.getDescription());
                product.setShortDescription(productDTO.getShortDescription());
                product.setIsFeatured(productDTO.getIsFeatured());
                product.setStatus(productDTO.getStatus());
                // TODO: Gán danh mục, thuộc tính, ảnh, variants nếu cần
                Product saved = repository.save(product);
                return convertToProductDTO(saved);
        }

        @Transactional
        public void deleteProduct(Long id) {
                repository.deleteById(id);
        }
}
