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

import java.util.stream.Collectors;

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
                        // Set temporary price
                        setTemporaryPriceForVariant(variant, saved);
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
                    // Set temporary price
                    setTemporaryPriceForVariant(variant, saved);
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
                        // Set temporary price
                        setTemporaryPriceForVariant(variant, saved);
                    });
                }
            });
        }
        return toDTO(saved);
    }

    // Helper: Set temporary price for variant
    private void setTemporaryPriceForVariant(ProductVariant variant, Promotion promotion) {
        if (promotion.getIsActive() != null && promotion.getIsActive()) {
            if (promotion.getType() == Promotion.PromotionType.PERCENTAGE) {
                variant.setDiscountPrice(variant.getPrice().multiply(
                    java.math.BigDecimal.valueOf(1 - promotion.getDiscountValue().doubleValue() / 100)
                ).setScale(2, java.math.RoundingMode.HALF_UP));
            } else {
                variant.setDiscountPrice(variant.getPrice().subtract(promotion.getDiscountValue()).max(java.math.BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP));
            }
            variant.setDiscountPriceStartAt(promotion.getStartDate());
            variant.setDiscountPriceEndAt(promotion.getEndDate());
            productVariantRepository.save(variant);
        }
    }

    // Helper: Reset temporary price for variant if no active promotion
    private void resetTemporaryPriceForVariant(ProductVariant variant) {
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
            variant.setDiscountPrice(null);
            variant.setDiscountPriceStartAt(null);
            variant.setDiscountPriceEndAt(null);
            productVariantRepository.save(variant);
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
        // Xóa hết PromotionProduct cũ
        java.util.List<PromotionProduct> oldPPs = promotionProductRepository.findAll().stream().filter(pp -> pp.getPromotion().getPromotionId().equals(promotion.getPromotionId())).collect(java.util.stream.Collectors.toList());
        for (PromotionProduct pp : oldPPs) {
            resetTemporaryPriceForVariant(pp.getVariant());
        }
        promotionProductRepository.deleteByPromotionId(promotion.getPromotionId());
        // Xử lý lại theo applyType mới
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
                    });
                }
            });
        }
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
        // Lấy danh sách variantIds nếu là single
        if (promotion.getPromotionProducts() != null) {
            dto.setVariantIds(promotion.getPromotionProducts().stream()
                .map(pp -> pp.getVariant().getVariantId())
                .collect(Collectors.toList()));
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
