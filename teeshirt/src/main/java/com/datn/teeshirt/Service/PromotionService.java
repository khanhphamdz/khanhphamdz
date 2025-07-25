package com.datn.teeshirt.Service;

import com.datn.teeshirt.DTO.PromotionDTO;
import com.datn.teeshirt.Entity.Promotion;
import com.datn.teeshirt.Entity.PromotionProduct;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Repository.PromotionRepository;
import com.datn.teeshirt.Repository.PromotionProductRepository;
import com.datn.teeshirt.Repository.ProductRepository;
import com.datn.teeshirt.Repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

<<<<<<< HEAD
import java.util.stream.Collectors;
=======
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public PromotionService(PromotionRepository promotionRepository, PromotionProductRepository promotionProductRepository, ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.promotionRepository = promotionRepository;
        this.promotionProductRepository = promotionProductRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    // Lấy tất cả promotion (có phân trang, tìm kiếm)
    public Page<PromotionDTO> getAll(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null && !name.trim().isEmpty()) {
            return promotionRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toDTO);
        }
        return promotionRepository.findAll(pageable).map(this::toDTO);
    }

    // Lấy promotion theo ID
    public PromotionDTO getById(Long id) {
        return promotionRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // Tạo mới promotion
    @Transactional
    public PromotionDTO create(PromotionDTO dto) {
        Promotion promotion = toEntity(dto);
        promotion.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
<<<<<<< HEAD
=======
        // Kiểm tra trùng promotion trước khi lưu
        if ("all".equalsIgnoreCase(dto.getApplyType())) {
            java.util.List<Long> allVariantIds = productVariantRepository.findAll().stream().map(v -> v.getVariantId()).toList();
            checkPromotionConflict(promotion, allVariantIds);
        } else if (dto.getVariantIds() != null && !dto.getVariantIds().isEmpty()) {
            checkPromotionConflict(promotion, dto.getVariantIds());
        } else if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            java.util.List<Long> catVariantIds = productRepository.findAll().stream()
                .filter(product -> product.getProductCategories() != null && product.getProductCategories().stream().anyMatch(pc -> dto.getCategoryIds().contains(pc.getCategory().getCategoryId())))
                .flatMap(product -> productVariantRepository.findByProduct_ProductId(product.getProductId()).stream())
                .map(v -> v.getVariantId()).toList();
            checkPromotionConflict(promotion, catVariantIds);
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        Promotion saved = promotionRepository.save(promotion);
        // Xử lý logic applyType
        if ("all".equalsIgnoreCase(dto.getApplyType())) {
            productRepository.findAll().forEach(product -> {
                if (Boolean.TRUE.equals(product.getStatus())) {
                    productVariantRepository.findByProduct_ProductId(product.getProductId()).forEach(variant -> {
                        PromotionProduct pp = PromotionProduct.builder()
                            .id(new PromotionProduct.PromotionProductId(saved.getPromotionId(), product.getProductId(), variant.getVariantId()))
                            .promotion(saved)
                            .product(product)
                            .variant(variant)
                            .build();
                        promotionProductRepository.save(pp);
<<<<<<< HEAD
                        // Set temporary price
                        setTemporaryPriceForVariant(variant, saved);
=======
                        // Đảm bảo luôn lấy promotion lớn nhất
                        resetTemporaryPriceForVariant(variant);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                    });
                }
            });
        } else if ("single".equalsIgnoreCase(dto.getApplyType())) {
            if (dto.getVariantIds() == null || dto.getVariantIds().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất một biến thể sản phẩm khi áp dụng cho sản phẩm cụ thể!");
            }
            for (Long variantId : dto.getVariantIds()) {
                ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
                if (variant != null) {
                    PromotionProduct pp = PromotionProduct.builder()
                        .id(new PromotionProduct.PromotionProductId(saved.getPromotionId(), variant.getProduct().getProductId(), variantId))
                        .promotion(saved)
                        .product(variant.getProduct())
                        .variant(variant)
                        .build();
                    promotionProductRepository.save(pp);
<<<<<<< HEAD
                    // Set temporary price
                    setTemporaryPriceForVariant(variant, saved);
=======
                    resetTemporaryPriceForVariant(variant);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                }
            }
        } else if ("category".equalsIgnoreCase(dto.getApplyType())) {
            if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất một danh mục khi áp dụng cho danh mục sản phẩm!");
            }
            productRepository.findAll().forEach(product -> {
                if (product.getProductCategories() != null && product.getProductCategories().stream().anyMatch(pc -> dto.getCategoryIds().contains(pc.getCategory().getCategoryId()))) {
                    productVariantRepository.findByProduct_ProductId(product.getProductId()).forEach(variant -> {
                        PromotionProduct pp = PromotionProduct.builder()
                            .id(new PromotionProduct.PromotionProductId(saved.getPromotionId(), product.getProductId(), variant.getVariantId()))
                            .promotion(saved)
                            .product(product)
                            .variant(variant)
                            .build();
                        promotionProductRepository.save(pp);
<<<<<<< HEAD
                        // Set temporary price
                        setTemporaryPriceForVariant(variant, saved);
=======
                        resetTemporaryPriceForVariant(variant);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                    });
                }
            });
        }
<<<<<<< HEAD
        return toDTO(saved);
=======
        return toDTO(promotionRepository.save(saved));
    }

    // Helper: Kiểm tra trùng promotion active trên cùng variant và giao thời gian
    private void checkPromotionConflict(Promotion promotion, List<Long> variantIds) {
        for (Long variantId : variantIds) {
            java.util.List<PromotionProduct> activePromos = promotionProductRepository.findByVariant_VariantId(variantId);
            for (PromotionProduct pp : activePromos) {
                Promotion other = pp.getPromotion();
                if (other.getPromotionId() != null && !other.getPromotionId().equals(promotion.getPromotionId())
                        && Boolean.TRUE.equals(other.getIsActive())) {
                    // Kiểm tra giao thời gian
                    boolean overlap = !(promotion.getEndDate().isBefore(other.getStartDate()) || promotion.getStartDate().isAfter(other.getEndDate()));
                    if (overlap) {
                        String message = String.format(
                            "Thời gian này đã có chương trình khuyến mãi khác: '%s' (Từ %s đến %s) áp dụng cho sản phẩm này!",
                            other.getName(),
                            other.getStartDate().toString(),
                            other.getEndDate().toString()
                        );
                        throw new IllegalArgumentException(message);
                    }
                }
            }
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    }

    // Helper: Set temporary price for variant
    private void setTemporaryPriceForVariant(ProductVariant variant, Promotion promotion) {
        if (promotion.getIsActive() != null && promotion.getIsActive()) {
<<<<<<< HEAD
            if (promotion.getType() == Promotion.PromotionType.PERCENTAGE) {
                variant.setDiscountPrice(variant.getPrice().multiply(
                    java.math.BigDecimal.valueOf(1 - promotion.getDiscountValue().doubleValue() / 100)
                ).setScale(2, java.math.RoundingMode.HALF_UP));
            } else {
                variant.setDiscountPrice(variant.getPrice().subtract(promotion.getDiscountValue()).max(java.math.BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP));
            }
=======
            BigDecimal discountPrice = null;
            if (promotion.getType() == Promotion.PromotionType.PERCENTAGE) {
                discountPrice = variant.getPrice().multiply(
                    java.math.BigDecimal.valueOf(1 - promotion.getDiscountValue().doubleValue() / 100)
                ).setScale(2, java.math.RoundingMode.HALF_UP);
            } else {
                discountPrice = variant.getPrice().subtract(promotion.getDiscountValue());
                if (discountPrice.compareTo(BigDecimal.ZERO) < 0) discountPrice = BigDecimal.ZERO;
                discountPrice = discountPrice.setScale(2, java.math.RoundingMode.HALF_UP);
            }
            // Nếu discountPrice >= price thì không set discount (tránh lỗi hiển thị)
            if (discountPrice.compareTo(variant.getPrice()) >= 0) {
                discountPrice = null;
            }
            variant.setDiscountPrice(discountPrice);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            variant.setDiscountPriceStartAt(promotion.getStartDate());
            variant.setDiscountPriceEndAt(promotion.getEndDate());
            productVariantRepository.save(variant);
        }
    }

    // Helper: Reset temporary price for variant if no active promotion
    private void resetTemporaryPriceForVariant(ProductVariant variant) {
<<<<<<< HEAD
        // Kiểm tra còn promotion nào active cho variant này không
        java.util.List<PromotionProduct> activePromos = promotionProductRepository.findByVariant_VariantId(variant.getVariantId());
        boolean hasActive = activePromos.stream().anyMatch(pp -> {
            Promotion promo = pp.getPromotion();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            return promo.getIsActive() != null && promo.getIsActive()
                && !now.isBefore(promo.getStartDate())
                && !now.isAfter(promo.getEndDate());
        });
        if (!hasActive) {
=======
        // Lấy tất cả promotion còn active cho variant này
        java.util.List<PromotionProduct> activePromos = promotionProductRepository.findByVariant_VariantId(variant.getVariantId());
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // Lọc ra các promotion còn hiệu lực
        java.util.List<Promotion> validPromos = activePromos.stream()
            .map(PromotionProduct::getPromotion)
            .filter(promo -> promo != null && promo.getIsActive() != null && promo.getIsActive()
                && !now.isBefore(promo.getStartDate()) && !now.isAfter(promo.getEndDate()))
            .toList();
        if (validPromos.isEmpty()) {
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
            variant.setDiscountPrice(null);
            variant.setDiscountPriceStartAt(null);
            variant.setDiscountPriceEndAt(null);
            productVariantRepository.save(variant);
<<<<<<< HEAD
=======
        } else {
            // Lấy promotion có discount lớn nhất
            Promotion bestPromo = validPromos.stream()
                .max(java.util.Comparator.comparing(Promotion::getDiscountValue))
                .orElse(null);
            if (bestPromo != null) {
                BigDecimal discountPrice = null;
                if (bestPromo.getType() == Promotion.PromotionType.PERCENTAGE) {
                    discountPrice = variant.getPrice().multiply(
                        java.math.BigDecimal.valueOf(1 - bestPromo.getDiscountValue().doubleValue() / 100)
                    ).setScale(2, java.math.RoundingMode.HALF_UP);
                } else {
                    discountPrice = variant.getPrice().subtract(bestPromo.getDiscountValue());
                    if (discountPrice.compareTo(BigDecimal.ZERO) < 0) discountPrice = BigDecimal.ZERO;
                    discountPrice = discountPrice.setScale(2, java.math.RoundingMode.HALF_UP);
                }
                // Nếu discountPrice >= price thì không set discount (tránh lỗi hiển thị)
                if (discountPrice.compareTo(variant.getPrice()) >= 0) {
                    discountPrice = null;
                }
                variant.setDiscountPrice(discountPrice);
                variant.setDiscountPriceStartAt(bestPromo.getStartDate());
                variant.setDiscountPriceEndAt(bestPromo.getEndDate());
                productVariantRepository.save(variant);
            }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        }
    }

    // Cập nhật promotion
    @Transactional
    public PromotionDTO update(Long id, PromotionDTO dto) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow();
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountValue(dto.getDiscountValue());
        promotion.setType(Promotion.PromotionType.valueOf(dto.getType().toUpperCase()));
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setIsActive(dto.getIsActive());
<<<<<<< HEAD
=======
        // Kiểm tra trùng promotion trước khi cập nhật
        if ("all".equalsIgnoreCase(dto.getApplyType())) {
            java.util.List<Long> allVariantIds = productVariantRepository.findAll().stream().map(v -> v.getVariantId()).toList();
            checkPromotionConflict(promotion, allVariantIds);
        } else if (dto.getVariantIds() != null && !dto.getVariantIds().isEmpty()) {
            checkPromotionConflict(promotion, dto.getVariantIds());
        } else if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            java.util.List<Long> catVariantIds = productRepository.findAll().stream()
                .filter(product -> product.getProductCategories() != null && product.getProductCategories().stream().anyMatch(pc -> dto.getCategoryIds().contains(pc.getCategory().getCategoryId())))
                .flatMap(product -> productVariantRepository.findByProduct_ProductId(product.getProductId()).stream())
                .map(v -> v.getVariantId()).toList();
            checkPromotionConflict(promotion, catVariantIds);
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        // Xóa hết PromotionProduct cũ
        java.util.List<PromotionProduct> oldPPs = promotionProductRepository.findAll().stream().filter(pp -> pp.getPromotion().getPromotionId().equals(promotion.getPromotionId())).collect(java.util.stream.Collectors.toList());
        for (PromotionProduct pp : oldPPs) {
            resetTemporaryPriceForVariant(pp.getVariant());
        }
        promotionProductRepository.deleteByPromotionId(promotion.getPromotionId());
        // Xử lý lại theo applyType mới
<<<<<<< HEAD
=======
        java.util.Set<Long> newVariantIds = new java.util.HashSet<>();
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        if ("all".equalsIgnoreCase(dto.getApplyType())) {
            productRepository.findAll().forEach(product -> {
                if (Boolean.TRUE.equals(product.getStatus())) {
                    productVariantRepository.findByProduct_ProductId(product.getProductId()).forEach(variant -> {
                        PromotionProduct pp = PromotionProduct.builder()
                            .id(new PromotionProduct.PromotionProductId(promotion.getPromotionId(), product.getProductId(), variant.getVariantId()))
                            .promotion(promotion)
                            .product(product)
                            .variant(variant)
                            .build();
                        promotionProductRepository.save(pp);
                        setTemporaryPriceForVariant(variant, promotion);
<<<<<<< HEAD
=======
                        newVariantIds.add(variant.getVariantId());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                    });
                }
            });
        } else if ("single".equalsIgnoreCase(dto.getApplyType())) {
            if (dto.getVariantIds() == null || dto.getVariantIds().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất một biến thể sản phẩm khi áp dụng cho sản phẩm cụ thể!");
            }
            for (Long variantId : dto.getVariantIds()) {
                ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
                if (variant != null) {
                    PromotionProduct pp = PromotionProduct.builder()
                        .id(new PromotionProduct.PromotionProductId(promotion.getPromotionId(), variant.getProduct().getProductId(), variantId))
                        .promotion(promotion)
                        .product(variant.getProduct())
                        .variant(variant)
                        .build();
                    promotionProductRepository.save(pp);
                    setTemporaryPriceForVariant(variant, promotion);
<<<<<<< HEAD
=======
                    newVariantIds.add(variantId);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                }
            }
        } else if ("category".equalsIgnoreCase(dto.getApplyType())) {
            if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất một danh mục khi áp dụng cho danh mục sản phẩm!");
            }
            productRepository.findAll().forEach(product -> {
                if (product.getProductCategories() != null && product.getProductCategories().stream().anyMatch(pc -> dto.getCategoryIds().contains(pc.getCategory().getCategoryId()))) {
                    productVariantRepository.findByProduct_ProductId(product.getProductId()).forEach(variant -> {
                        PromotionProduct pp = PromotionProduct.builder()
                            .id(new PromotionProduct.PromotionProductId(promotion.getPromotionId(), product.getProductId(), variant.getVariantId()))
                            .promotion(promotion)
                            .product(product)
                            .variant(variant)
                            .build();
                        promotionProductRepository.save(pp);
                        setTemporaryPriceForVariant(variant, promotion);
<<<<<<< HEAD
=======
                        newVariantIds.add(variant.getVariantId());
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
                    });
                }
            });
        }
<<<<<<< HEAD
=======
        // Sau khi cập nhật, reset lại discount cho các variant KHÔNG còn thuộc chương trình này
        if (!"all".equalsIgnoreCase(dto.getApplyType())) {
            java.util.List<ProductVariant> allVariants = productVariantRepository.findAll();
            for (ProductVariant variant : allVariants) {
                if (!newVariantIds.contains(variant.getVariantId())) {
                    resetTemporaryPriceForVariant(variant);
                }
            }
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        return toDTO(promotionRepository.save(promotion));
    }

    // Xóa promotion
    @Transactional
    public void delete(Long id) {
        // Xóa hết PromotionProduct liên quan trước khi xóa promotion
        java.util.List<PromotionProduct> pps = promotionProductRepository.findAll().stream().filter(pp -> pp.getPromotion().getPromotionId().equals(id)).collect(java.util.stream.Collectors.toList());
        for (PromotionProduct pp : pps) {
            resetTemporaryPriceForVariant(pp.getVariant());
        }
        promotionProductRepository.deleteByPromotionId(id);
        promotionRepository.deleteById(id);
<<<<<<< HEAD
=======
        // Nếu không còn promotion nào, reset toàn bộ discount_price về null
        if (promotionRepository.count() == 0) {
            java.util.List<ProductVariant> allVariants = productVariantRepository.findAll();
            for (ProductVariant variant : allVariants) {
                variant.setDiscountPrice(null);
                variant.setDiscountPriceStartAt(null);
                variant.setDiscountPriceEndAt(null);
                productVariantRepository.save(variant);
            }
        }
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
    }

    // Đổi trạng thái active/inactive
    @Transactional
    public PromotionDTO setActive(Long id, boolean active) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow();
        promotion.setIsActive(active);
        return toDTO(promotionRepository.save(promotion));
    }

    // Chuyển đổi Entity -> DTO
    private PromotionDTO toDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setType(promotion.getType().name());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setIsActive(promotion.getIsActive());
<<<<<<< HEAD
        // Lấy danh sách variantIds nếu là single
        if (promotion.getPromotionProducts() != null) {
            dto.setVariantIds(promotion.getPromotionProducts().stream()
                .map(pp -> pp.getVariant().getVariantId())
                .collect(Collectors.toList()));
=======
        // Xác định applyType và trả về đầy đủ các trường
        if (promotion.getPromotionProducts() != null && !promotion.getPromotionProducts().isEmpty()) {
            List<Long> variantIds = promotion.getPromotionProducts().stream()
                .map(pp -> pp.getVariant().getVariantId())
                .collect(Collectors.toList());
            List<Long> productIds = promotion.getPromotionProducts().stream()
                .map(pp -> pp.getProduct().getProductId())
                .distinct()
                .collect(Collectors.toList());
            List<Long> allVariantIds = productVariantRepository.findAll().stream().map(v -> v.getVariantId()).collect(Collectors.toList());
            if (variantIds.size() == allVariantIds.size() && allVariantIds.containsAll(variantIds)) {
                dto.setApplyType("all");
                dto.setVariantIds(variantIds);
                dto.setCategoryIds(null);
                dto.setProductIds(productRepository.findAll().stream().map(Product::getProductId).collect(Collectors.toList()));
            } else {
                List<Long> categoryIds = productRepository.findAll().stream()
                    .filter(product -> productIds.contains(product.getProductId()))
                    .flatMap(product -> (product.getProductCategories() != null ? product.getProductCategories().stream() : java.util.stream.Stream.empty()))
                    .map(pc -> pc.getCategory().getCategoryId())
                    .distinct()
                    .collect(Collectors.toList());
                List<Long> catVariantIds = productRepository.findAll().stream()
                    .filter(product -> product.getProductCategories() != null && product.getProductCategories().stream().anyMatch(pc -> categoryIds.contains(pc.getCategory().getCategoryId())))
                    .flatMap(product -> productVariantRepository.findByProduct_ProductId(product.getProductId()).stream())
                    .map(v -> v.getVariantId()).collect(Collectors.toList());
                if (variantIds.size() == catVariantIds.size() && catVariantIds.containsAll(variantIds)) {
                    dto.setApplyType("category");
                    dto.setCategoryIds(categoryIds);
                    dto.setVariantIds(variantIds);
                    dto.setProductIds(productIds);
                } else {
                    dto.setApplyType("single");
                    dto.setVariantIds(variantIds);
                    dto.setCategoryIds(null);
                    dto.setProductIds(productIds);
                }
            }
        } else {
            dto.setApplyType("all");
            dto.setVariantIds(null);
            dto.setCategoryIds(null);
            dto.setProductIds(null);
>>>>>>> b701f766cc9f1669099fbfcef74506c420c14a05
        }
        return dto;
    }

    // Chuyển đổi DTO -> Entity
    private Promotion toEntity(PromotionDTO dto) {
        return Promotion.builder()
                .promotionId(dto.getPromotionId())
                .name(dto.getName())
                .description(dto.getDescription())
                .discountValue(dto.getDiscountValue())
                .type(Promotion.PromotionType.valueOf(dto.getType().toUpperCase()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(dto.getIsActive())
                .build();
    }
} 
