package com.datn.teeshirt.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.Entity.Category;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.ProductImage;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Repository.CategoryRepository;
import com.datn.teeshirt.Repository.ColorRepository;
import com.datn.teeshirt.Repository.MaterialRepository;
import com.datn.teeshirt.Repository.ProductImageRepository;
import com.datn.teeshirt.Repository.ProductRepository;
import com.datn.teeshirt.Repository.ProductVariantRepository;
import com.datn.teeshirt.Repository.SizeRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ColorRepository colorRepository;
    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private MaterialRepository materialRepository;

    public Page<ProductDTO> findAll(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::toProductDTO);
    }

    // 1. Lấy danh sách sản phẩm (có phân trang, lọc, tìm kiếm)
    public Page<ProductDTO> getProducts(int page, int size, String keyword, Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, Boolean status) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.search(keyword, pageable).map(this::toProductDTO);
        }
        return productRepository.filter(categoryId, status, minPrice, maxPrice, pageable).map(this::toProductDTO);
    }

    // 2. Lấy chi tiết sản phẩm
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return toProductDTO(product);
    }

    // 3. Tạo mới sản phẩm
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setShortDescription(productDTO.getShortDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setStatus(productDTO.getStatus());
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow();
            product.setCategory(category);
        }
        Product saved = productRepository.save(product);
        return toProductDTO(saved);
    }

    // 4. Cập nhật sản phẩm
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setShortDescription(productDTO.getShortDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setStatus(productDTO.getStatus());
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow();
            product.setCategory(category);
        }
        Product saved = productRepository.save(product);
        return toProductDTO(saved);
    }

    // 5. Xóa sản phẩm (mềm)
    @Transactional
    public void softDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
    }

    // 6. Lấy danh sách biến thể của sản phẩm
    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProduct_ProductId(productId)
                .stream().map(this::toProductVariantDTO).collect(Collectors.toList());
    }

    // 7. Thêm biến thể cho sản phẩm
    @Transactional
    public ProductVariantDTO addVariantToProduct(Long productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId).orElseThrow();
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(variantDTO.getSku());
        variant.setBarcode(variantDTO.getBarcode());
        variant.setPrice(variantDTO.getPrice());
        variant.setDiscountPrice(variantDTO.getDiscountPrice());
        variant.setDiscountPriceStartAt(variantDTO.getDiscountPriceStartAt());
        variant.setDiscountPriceEndAt(variantDTO.getDiscountPriceEndAt());
        variant.setQuantityInStock(variantDTO.getQuantityInStock());
        variant.setIsActive(variantDTO.getIsActive());
        if (variantDTO.getColorId() != null) {
            variant.setColor(colorRepository.findById(variantDTO.getColorId()).orElse(null));
        }
        if (variantDTO.getSizeId() != null) {
            variant.setSize(sizeRepository.findById(variantDTO.getSizeId()).orElse(null));
        }
        if (variantDTO.getMaterialId() != null) {
            variant.setMaterial(materialRepository.findById(variantDTO.getMaterialId()).orElse(null));
        }
        ProductVariant saved = productVariantRepository.save(variant);
        return toProductVariantDTO(saved);
    }

    // 8. Cập nhật biến thể
    @Transactional
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO variantDTO) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElseThrow();
        variant.setSku(variantDTO.getSku());
        variant.setBarcode(variantDTO.getBarcode());
        variant.setPrice(variantDTO.getPrice());
        variant.setDiscountPrice(variantDTO.getDiscountPrice());
        variant.setDiscountPriceStartAt(variantDTO.getDiscountPriceStartAt());
        variant.setDiscountPriceEndAt(variantDTO.getDiscountPriceEndAt());
        variant.setQuantityInStock(variantDTO.getQuantityInStock());
        variant.setIsActive(variantDTO.getIsActive());
        if (variantDTO.getColorId() != null) {
            variant.setColor(colorRepository.findById(variantDTO.getColorId()).orElse(null));
        }
        if (variantDTO.getSizeId() != null) {
            variant.setSize(sizeRepository.findById(variantDTO.getSizeId()).orElse(null));
        }
        if (variantDTO.getMaterialId() != null) {
            variant.setMaterial(materialRepository.findById(variantDTO.getMaterialId()).orElse(null));
        }
        ProductVariant saved = productVariantRepository.save(variant);
        return toProductVariantDTO(saved);
    }

    // 9. Xóa biến thể
    @Transactional
    public void deleteVariant(Long variantId) {
        productVariantRepository.deleteById(variantId);
    }

    // 10. Quản lý ảnh sản phẩm
    public List<ProductImageDTO> getImagesByProductId(Long productId) {
        return productImageRepository.findByProduct_ProductId(productId)
                .stream().map(this::toProductImageDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductImageDTO addImageToProduct(Long productId, ProductImageDTO imageDTO) {
        Product product = productRepository.findById(productId).orElseThrow();
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageDTO.getImageUrl());
        image.setImageType(imageDTO.getImageType());
        ProductImage saved = productImageRepository.save(image);
        return toProductImageDTO(saved);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        productImageRepository.deleteById(imageId);
    }

    // 11. Lấy sản phẩm mới nhất, nổi bật, khuyến mãi...
    public List<ProductDTO> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findLatestProducts(pageable)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getDiscountedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findDiscountedProducts(pageable)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    // ==================== Mapping ====================
    private ProductDTO toProductDTO(Product product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .variants(product.getVariants() != null
                        ? product.getVariants().stream().map(this::toProductVariantDTO).collect(Collectors.toList())
                        : null)
                .images(product.getImages() != null
                        ? product.getImages().stream().map(this::toProductImageDTO).collect(Collectors.toList())
                        : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductVariantDTO toProductVariantDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .variantId(variant.getVariantId())
                .productId(variant.getProduct() != null ? variant.getProduct().getProductId() : null)
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .colorId(variant.getColor() != null ? variant.getColor().getColorId() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .sizeId(variant.getSize() != null ? variant.getSize().getSizeId() : null)
                .sizeName(variant.getSize() != null ? variant.getSize().getName() : null)
                .materialId(variant.getMaterial() != null ? variant.getMaterial().getMaterialId() : null)
                .materialName(variant.getMaterial() != null ? variant.getMaterial().getName() : null)
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .discountPriceStartAt(variant.getDiscountPriceStartAt())
                .discountPriceEndAt(variant.getDiscountPriceEndAt())
                .quantityInStock(variant.getQuantityInStock())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    private ProductImageDTO toProductImageDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .imageId(image.getImageId())
                .productId(image.getProduct() != null ? image.getProduct().getProductId() : null)
                .variantId(image.getVariant() != null ? image.getVariant().getVariantId() : null)
                .imageUrl(image.getImageUrl())
                .imageType(image.getImageType())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
