package com.datn.teeshirt.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.datn.teeshirt.DTO.CategoryDTO;
import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.Entity.Category;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.ProductCategory;
import com.datn.teeshirt.Entity.ProductImage;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Repository.CategoryRepository;
import com.datn.teeshirt.Repository.ColorRepository;
import com.datn.teeshirt.Repository.MaterialRepository;
import com.datn.teeshirt.Repository.ProductCategoryRepository;
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
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    public Page<ProductDTO> findAll(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::toProductDTO);
    }

    public List<ProductDTO> findAll2() {
        List<Product> list = productRepository.findAll();
        return list.stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    // 1. Tìm kiếm sản phẩm
    public List<ProductDTO> searchProducts(String keyword) {
        List<ProductDTO> products = productRepository.search(keyword)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
        return products;
    }

    // 1. Lấy danh sách sản phẩm (có phân trang, lọc, tìm kiếm)
    public Page<ProductDTO> getProducts(int page, int size, String keyword, Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, Boolean status) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.search2(keyword, pageable).map(this::toProductDTO);
        }
        return productRepository.filter(categoryId, status, minPrice, maxPrice, pageable).map(this::toProductDTO);
    }

    // 1.2. Lấy danh sách sản phẩm với filter đầy đủ (bao gồm color và size)
    public Page<ProductDTO> getProducts(int page, int size, String keyword, Long categoryId, Long colorId, Long sizeId,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean status) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository
                    .searchWithFilters(keyword, categoryId, colorId, sizeId, status, null, null, pageable)
                    .map(this::toProductDTO);
        }
        return productRepository.filterWithColorAndSize(categoryId, colorId, sizeId, status, null, null, pageable)
                .map(this::toProductDTO);
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
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : true);
        product.setMaterial(materialRepository.findById(productDTO.getMaterialId()).orElseThrow());
        Product saved = productRepository.save(product);

        // Thêm category nếu có
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow();
            ProductCategory productCategory = ProductCategory.builder()
                    .product(saved)
                    .category(category)
                    .build();
            productCategoryRepository.save(productCategory);
        }

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
        product.setMaterial(materialRepository.findById(productDTO.getMaterialId()).orElseThrow());
        Product saved = productRepository.save(product);
        return toProductDTO(saved);
    }

    // 5. Xóa sản phẩm (mềm)
    @Transactional
    public void softDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
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
        if (variantDTO.getColorId() != null && variantDTO.getSizeId() != null) {
            boolean exists = productVariantRepository.existsByProduct_ProductIdAndColor_ColorIdAndSize_SizeId(
                    productId, variantDTO.getColorId(), variantDTO.getSizeId());
            if (exists) {
                throw new RuntimeException("Đã tồn tại biến thể với màu sắc và kích cỡ này!");
            }
        }
        Product product = productRepository.findById(productId).orElseThrow();
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setName(variantDTO.getName());
        variant.setSku(variantDTO.getSku());
        if (variantDTO.getBarcode() == null || variantDTO.getBarcode().isEmpty()) {
            String barcode;
            do {
                barcode = generateBarcode();
            } while (productVariantRepository.existsByBarcode(barcode));
            variant.setBarcode(barcode);
        } else {
            if (productVariantRepository.existsByBarcode(variantDTO.getBarcode())) {
                throw new RuntimeException("Barcode đã tồn tại!");
            }
            variant.setBarcode(variantDTO.getBarcode());
        }
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
        ProductVariant saved = productVariantRepository.save(variant);
        return toProductVariantDTO(saved);
    }

    // 8. Cập nhật biến thể
    @Transactional
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO variantDTO) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElseThrow();
        variant.setName(variantDTO.getName());
        variant.setSku(variantDTO.getSku());
        if (variantDTO.getBarcode() == null || variantDTO.getBarcode().isEmpty()) {
            String barcode;
            do {
                barcode = generateBarcode();
            } while (productVariantRepository.existsByBarcode(barcode));
            variant.setBarcode(barcode);
        } else {
            if (!variantDTO.getBarcode().equals(variant.getBarcode())
                    && productVariantRepository.existsByBarcode(variantDTO.getBarcode())) {
                throw new RuntimeException("Barcode đã tồn tại!");
            }
            variant.setBarcode(variantDTO.getBarcode());
        }
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

    // Lấy ảnh thumbnail của sản phẩm
    public List<ProductImageDTO> getProductThumbnails(Long productId) {
        return productImageRepository.findByProduct_ProductIdAndImageType(productId, "thumbnail")
                .stream().map(this::toProductImageDTO).collect(Collectors.toList());
    }

    // Lấy tất cả ảnh của sản phẩm
    public List<ProductImageDTO> getAllProductImage(Long productId) {
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
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh với ID: " + imageId));

        try {
            // Xóa file từ Cloudinary
            boolean cloudinaryDeleted = cloudinaryService.deleteFile(image.getImageUrl());

            // Xóa record từ database
            productImageRepository.deleteById(imageId);

            // Log kết quả
            if (!cloudinaryDeleted) {
                System.err.println("Cảnh báo: Không thể xóa file từ Cloudinary: " + image.getImageUrl());
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa ảnh: " + e.getMessage());
        }
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

    public Page<ProductDTO> filterProducts(
            Long categoryId, Boolean status, String keyword, String sort, int page, int size) {
        Pageable pageable;
        switch (sort != null ? sort : "") {
            case "name-asc":
                pageable = PageRequest.of(page, size, Sort.by("name").ascending());
                break;
            case "name-desc":
                pageable = PageRequest.of(page, size, Sort.by("name").descending());
                break;
            case "price-asc":
                pageable = PageRequest.of(page, size, Sort.by("basePrice").ascending());
                break;
            case "price-desc":
                pageable = PageRequest.of(page, size, Sort.by("basePrice").descending());
                break;
            case "date-asc":
                pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
                break;
            case "date-desc":
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                break;
            default:
                pageable = PageRequest.of(page, size);
                break;
        }
        if (keyword != null && !keyword.isEmpty()) {
            List<Product> products = productRepository.search(keyword);
            List<ProductDTO> dtos = products.stream().map(this::toProductDTO).collect(Collectors.toList());
            int start = Math.min(page * size, dtos.size());
            int end = Math.min((page + 1) * size, dtos.size());
            return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
        }
        return productRepository.filter(categoryId, status, null, null, pageable).map(this::toProductDTO);
    }

    public List<CategoryDTO> getAllCategoryDTOs() {
        List<Category> allCategories = categoryRepository.findAll();

        // Tạo map để dễ dàng tìm kiếm
        Map<Long, CategoryDTO> categoryMap = new HashMap<>();

        // Chuyển đổi tất cả category thành DTO
        for (Category category : allCategories) {
            CategoryDTO dto = CategoryDTO.builder()
                    .categoryId(category.getCategoryId())
                    .name(category.getName())
                    .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                    .children(new ArrayList<>())
                    .build();
            categoryMap.put(category.getCategoryId(), dto);
        }

        // Xây dựng cấu trúc cha-con
        List<CategoryDTO> rootCategories = new ArrayList<>();
        for (CategoryDTO dto : categoryMap.values()) {
            if (dto.getParentId() == null) {
                // Đây là danh mục gốc
                rootCategories.add(dto);
            } else {
                // Đây là danh mục con, thêm vào parent
                CategoryDTO parent = categoryMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootCategories;
    }

    // Helper method để lấy tất cả category IDs từ cấu trúc cha-con
    private List<Long> getAllCategoryIds(List<CategoryDTO> categories) {
        List<Long> allIds = new ArrayList<>();
        for (CategoryDTO category : categories) {
            allIds.add(category.getCategoryId());
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                allIds.addAll(getAllCategoryIds(category.getChildren()));
            }
        }
        return allIds;
    }

    // Helper method để lấy tất cả category IDs phẳng (không phân cấp)
    public List<Long> getAllFlatCategoryIds() {
        List<CategoryDTO> hierarchicalCategories = getAllCategoryDTOs();
        return getAllCategoryIds(hierarchicalCategories);
    }

    // Cập nhật danh mục sản phẩm
    @Transactional
    public void updateProductCategories(Long productId, List<Long> categoryIds) {
        Product product = productRepository.findById(productId).orElseThrow();

        // Xóa tất cả danh mục hiện tại của sản phẩm
        productCategoryRepository.deleteByProduct_ProductId(productId);

        // Thêm danh mục mới
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category != null) {
                    ProductCategory productCategory = ProductCategory.builder()
                            .product(product)
                            .category(category)
                            .build();
                    productCategoryRepository.save(productCategory);
                }
            }
        }
    }

    // Lấy danh sách category IDs của sản phẩm
    public List<Long> getProductCategoryIds(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        if (product.getProductCategories() != null) {
            return product.getProductCategories().stream()
                    .map(pc -> pc.getCategory().getCategoryId())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // ==================== Mapping ====================
    private ProductDTO toProductDTO(Product product) {
        // Lấy danh sách categories của sản phẩm
        List<CategoryDTO> productCategories = new ArrayList<>();
        if (product.getProductCategories() != null) {
            productCategories = product.getProductCategories().stream()
                    .map(pc -> CategoryDTO.builder()
                            .categoryId(pc.getCategory().getCategoryId())
                            .name(pc.getCategory().getName())
                            .parentId(
                                    pc.getCategory().getParent() != null ? pc.getCategory().getParent().getCategoryId()
                                            : null)
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .materialId(product.getMaterial() != null ? product.getMaterial().getMaterialId() : null)
                .materialName(product.getMaterial() != null ? product.getMaterial().getName() : null)
                .categories(productCategories)
                .images(getAllProductImage(product.getProductId()))
                .variants(product.getVariants() != null
                        ? product.getVariants().stream().map(this::toProductVariantDTO).collect(Collectors.toList())
                        : new ArrayList<>())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .averageRating(
                        product.getReviews() != null && !product.getReviews().isEmpty()
                                ? product.getReviews().stream()
                                        .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0).average().orElse(0)
                                : null)
                .build();
    }

    private ProductVariantDTO toProductVariantDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .variantId(variant.getVariantId())
                .productId(variant.getProduct() != null ? variant.getProduct().getProductId() : null)
                .name(variant.getName())
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .colorId(variant.getColor() != null ? variant.getColor().getColorId() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .sizeId(variant.getSize() != null ? variant.getSize().getSizeId() : null)
                .sizeName(variant.getSize() != null ? variant.getSize().getName() : null)
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .discountPriceStartAt(variant.getDiscountPriceStartAt())
                .discountPriceEndAt(variant.getDiscountPriceEndAt())
                .quantityInStock(variant.getQuantityInStock())
                .isActive(variant.getIsActive())
                .images(getImagesByVariantId(variant.getVariantId()))
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

    // lấy chi tiết biến thể theo id
    public ProductVariantDTO getVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElseThrow();
        return toProductVariantDTO(variant);
    }

    // danh sách hình ảnh biến thể theo id
    public List<ProductImageDTO> getImagesByVariantId(Long variantId) {
        return productImageRepository.findByVariant_VariantId(variantId)
                .stream().map(this::toProductImageDTO).collect(Collectors.toList());
    }

    // Upload ảnh biến thể
    @Transactional
    public List<ProductImageDTO> uploadVariantImages(Long variantId, MultipartFile[] images) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        List<ProductImageDTO> uploadedImages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            MultipartFile file = images[i];
            if (file != null && !file.isEmpty()) {
                try {
                    // Upload lên Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file, "variants");

                    // Lưu vào database
                    ProductImage image = new ProductImage();
                    image.setProduct(variant.getProduct());
                    image.setVariant(variant);
                    image.setImageUrl(imageUrl);
                    image.setImageType("variant");

                    ProductImage savedImage = productImageRepository.save(image);
                    uploadedImages.add(toProductImageDTO(savedImage));

                } catch (IOException e) {
                    errorMessages.add("File " + (i + 1) + " (" + file.getOriginalFilename() + "): " + e.getMessage());
                } catch (Exception e) {
                    errorMessages.add("File " + (i + 1) + " (" + file.getOriginalFilename() + "): Lỗi không xác định");
                }
            }
        }

        // Nếu có lỗi, throw exception với thông tin chi tiết
        if (!errorMessages.isEmpty()) {
            throw new RuntimeException("Lỗi khi upload một số file: " + String.join("; ", errorMessages));
        }

        return uploadedImages;
    }

    // Upload ảnh sản phẩm chính
    @Transactional
    public List<ProductImageDTO> uploadProductImages(Long productId, MultipartFile[] images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        List<ProductImageDTO> uploadedImages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            MultipartFile file = images[i];
            if (file != null && !file.isEmpty()) {
                try {
                    // Upload lên Cloudinary
                    String imageUrl = cloudinaryService.uploadFile(file, "products");

                    // Lưu vào database
                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setVariant(null); // Ảnh sản phẩm chính
                    image.setImageUrl(imageUrl);
                    image.setImageType("thumbnail"); // Thay đổi từ "product" thành "thumbnail"

                    ProductImage savedImage = productImageRepository.save(image);
                    uploadedImages.add(toProductImageDTO(savedImage));

                } catch (IOException e) {
                    errorMessages.add("File " + (i + 1) + " (" + file.getOriginalFilename() + "): " + e.getMessage());
                } catch (Exception e) {
                    errorMessages.add("File " + (i + 1) + " (" + file.getOriginalFilename() + "): Lỗi không xác định");
                }
            }
        }

        // Nếu có lỗi, throw exception với thông tin chi tiết
        if (!errorMessages.isEmpty()) {
            throw new RuntimeException("Lỗi khi upload một số file: " + String.join("; ", errorMessages));
        }

        return uploadedImages;
    }

    // Xóa ảnh sản phẩm
    @Transactional
    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElseThrow();

        // Xóa ảnh từ Cloudinary
        if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
            try {
                cloudinaryService.deleteFile(image.getImageUrl());
            } catch (Exception e) {
                // Log lỗi nhưng không throw exception để tránh ảnh hưởng đến việc xóa record
                System.err.println("Lỗi khi xóa ảnh từ Cloudinary: " + e.getMessage());
            }
        }

        productImageRepository.delete(image);
    }

    // Tạo biến thể tự động dựa trên thuộc tính sản phẩm
    @Transactional
    public List<ProductVariantDTO> generateVariants(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<com.datn.teeshirt.Entity.Color> colors = colorRepository.findAll();
        List<com.datn.teeshirt.Entity.Size> sizes = sizeRepository.findAll();
        List<ProductVariant> existingVariants = productVariantRepository.findByProduct_ProductId(productId);
        List<String> existingCombinations = existingVariants.stream()
                .map(v -> v.getColor().getColorId() + "-" + v.getSize().getSizeId())
                .toList();
        List<ProductVariantDTO> generatedVariants = new ArrayList<>();
        for (com.datn.teeshirt.Entity.Color color : colors) {
            for (com.datn.teeshirt.Entity.Size size : sizes) {
                String key = color.getColorId() + "-" + size.getSizeId();
                if (!existingCombinations.contains(key)) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setName(product.getName() + " - " + color.getName() + " - " + size.getName());
                    variant.setSku(generateSKU(product, color, size));
                    variant.setBarcode(generateBarcode());
                    variant.setPrice(product.getBasePrice());
                    variant.setQuantityInStock(0);
                    variant.setIsActive(true);
                    variant.setColor(color);
                    variant.setSize(size);
                    ProductVariant saved = productVariantRepository.save(variant);
                    generatedVariants.add(toProductVariantDTO(saved));
                }
            }
        }
        return generatedVariants;
    }

    private String generateSKU(Product product, com.datn.teeshirt.Entity.Color color,
            com.datn.teeshirt.Entity.Size size) {
        return String.format("%s-%s-%s", product.getProductId(),
                color.getName().substring(0, Math.min(3, color.getName().length())),
                size.getName().substring(0, Math.min(3, size.getName().length())));
    }

    // Áp dụng hàng loạt - Chỉnh sửa giá
    @Transactional
    public void bulkUpdatePrice(List<Long> variantIds, Double price) {
        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);

        for (ProductVariant variant : variants) {
            if (price != null) {
                variant.setPrice(BigDecimal.valueOf(price));
            }
        }

        productVariantRepository.saveAll(variants);
    }

    // Áp dụng hàng loạt - Chỉnh sửa số lượng
    @Transactional
    public void bulkUpdateStock(List<Long> variantIds, Integer stock) {
        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);

        for (ProductVariant variant : variants) {
            if (stock != null) {
                variant.setQuantityInStock(stock);
            }
        }

        productVariantRepository.saveAll(variants);
    }

    // Áp dụng hàng loạt - Xóa biến thể
    @Transactional
    public void bulkDeleteVariants(List<Long> variantIds) {
        // Xóa ảnh của các biến thể trước
        for (Long variantId : variantIds) {
            List<ProductImage> images = productImageRepository.findByVariant_VariantId(variantId);
            for (ProductImage image : images) {
                try {
                    cloudinaryService.deleteFile(image.getImageUrl());
                } catch (Exception e) {
                    System.err.println("Lỗi khi xóa ảnh từ Cloudinary: " + e.getMessage());
                }
            }
            productImageRepository.deleteAll(images);
        }

        // Xóa biến thể
        productVariantRepository.deleteAllById(variantIds);
    }

    // Helper methods
    private String generateBarcode() {
        String barcode;
        do {
            barcode = "BC" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        } while (productVariantRepository.existsByBarcode(barcode));
        return barcode;
    }

    public List<com.datn.teeshirt.Entity.Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    public List<ProductDTO> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getProductCategories() == null || product.getProductCategories().isEmpty()) {
            return new ArrayList<>();
        }
        // Lấy category đầu tiên của sản phẩm làm tiêu chí liên quan
        Long categoryId = product.getProductCategories().get(0).getCategory().getCategoryId();
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> related = productRepository.filter(categoryId, true, null, null, pageable)
                .stream().filter(p -> !p.getProductId().equals(productId)).limit(limit).collect(Collectors.toList());
        return related.stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    // Lấy sản phẩm liên quan - ưu tiên cùng danh mục, sau đó random
    public List<ProductDTO> getRelatedProductss(Long productId, int limit) {
        try {
            // Lấy thông tin sản phẩm hiện tại
            Product currentProduct = productRepository.findById(productId).orElse(null);

            List<Product> relatedProducts = new ArrayList<>();

            // Nếu sản phẩm có danh mục, tìm sản phẩm cùng danh mục trước
            if (currentProduct != null && currentProduct.getProductCategories() != null
                    && !currentProduct.getProductCategories().isEmpty()) {

                Long categoryId = currentProduct.getProductCategories().get(0).getCategory().getCategoryId();
                List<Product> sameCategoryProducts = productRepository.findByCategoryIdExcludingCurrent(categoryId,
                        productId);

                // Random và lấy một số sản phẩm cùng danh mục
                Collections.shuffle(sameCategoryProducts);
                int sameCategoryCount = Math.min(limit, sameCategoryProducts.size());
                relatedProducts.addAll(sameCategoryProducts.subList(0, sameCategoryCount));
            }

            // Nếu chưa đủ số lượng, bổ sung thêm sản phẩm ngẫu nhiên khác
            if (relatedProducts.size() < limit) {
                int remaining = limit - relatedProducts.size();

                List<Product> allOtherProducts = productRepository.findAllActiveExcludingCurrent(productId);

                // Loại bỏ các sản phẩm đã có trong danh sách
                allOtherProducts = allOtherProducts.stream()
                        .filter(p -> relatedProducts.stream()
                                .noneMatch(rp -> rp.getProductId().equals(p.getProductId())))
                        .collect(Collectors.toList());

                // Random và lấy thêm
                Collections.shuffle(allOtherProducts);
                int additionalCount = Math.min(remaining, allOtherProducts.size());
                relatedProducts.addAll(allOtherProducts.subList(0, additionalCount));
            }

            // Random lại toàn bộ danh sách để không có pattern cố định
            Collections.shuffle(relatedProducts);

            // Chuyển đổi sang DTO và trả về
            return relatedProducts.stream()
                    .limit(limit)
                    .map(this::toProductDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // Fallback: trả về danh sách rỗng nếu có lỗi
            System.err.println("Lỗi khi lấy sản phẩm liên quan: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Lấy sản phẩm theo barcode
    public ProductDTO getProductByBarcode(String barcode) {
        ProductVariant variant = productVariantRepository.findByBarcode(barcode).orElse(null);
        if (variant == null) return null;
        Product product = variant.getProduct();
        return toProductDTO(product);
    }

    // Overload: Lấy danh sách sản phẩm cho counter-sale (không cần filter nâng cao)
    public List<ProductDTO> getProducts(int page, int size, String keyword, Long categoryId) {
        // Giả sử chỉ lọc theo keyword và categoryId, phân trang
        // Có thể dùng repository custom hoặc filter lại từ danh sách
        // Đây là ví dụ đơn giản, bạn nên tối ưu lại nếu cần
        List<Product> products = productRepository.findAll();
        return products.stream()
            .filter(p -> (keyword == null || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                && (categoryId == null || p.getProductCategories().stream().anyMatch(c -> c.getCategory().getCategoryId().equals(categoryId))))
            .skip(page * size)
            .limit(size)
            .map(this::toProductDTO)
            .collect(Collectors.toList());
    }
}
