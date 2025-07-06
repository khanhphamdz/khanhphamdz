package com.datn.teeshirt.Controller.API;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.DTO.ProductImageDTO;
import com.datn.teeshirt.DTO.ProductVariantDTO;
import com.datn.teeshirt.Service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductRestController {
    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllProducts(@RequestParam(value = "page", defaultValue = "0") int page) {
        Page<ProductDTO> products = productService.findAll(page);
        if (products == null || products.isEmpty()) {
            return ResponseEntity.ok(new ResponseObject("false", "No products found", null));
        }
        return ResponseEntity.ok(new ResponseObject("ok", "No products found", products));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam("keyword") String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        if (products == null || products.isEmpty()) {
            return ResponseEntity.ok(new ResponseObject("false", "No products found", null));
        }
        return ResponseEntity.ok(new ResponseObject("false", "No products found", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/variant/{id}")
    public ResponseEntity<ResponseObject> getVariantByProduct(@PathVariable("id") Long id) {
        ProductVariantDTO variant = productService.getVariantById(id);
        List<ProductImageDTO> images = productService.getImagesByVariantId(id);

        Map<String, Object> response = Map.of(
                "variant", variant,
                "images", images);

        if (variant == null) {
            return ResponseEntity.ok(
                    new ResponseObject("error", "Không tìm thấy biến thể", null));
        }
        return ResponseEntity.ok(
                new ResponseObject("ok", "Tìm kiếm thành công", response));
    }

    // thêm biến thể mới
    @PostMapping("/variant/add")
    public ResponseEntity<ResponseObject> addVariant(@ModelAttribute ProductVariantDTO variantDTO) {
        try {
            // lưu biến thể
            ProductVariantDTO productVariantDTO = productService.addVariantToProduct(variantDTO.getProductId(),
                    variantDTO);
            return ResponseEntity.ok(new ResponseObject("ok", "Thêm biến thể thành công!", productVariantDTO));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("false", "Thêm biến thể thất bại:", e.getMessage()));
        }
    }

    // Cập nhật biến thể
    @PutMapping("/variant/{id}")
    public ResponseEntity<ResponseObject> updateVariant(@PathVariable Long id,
            @RequestBody ProductVariantDTO variantDTO) {
        try {
            variantDTO.setVariantId(id);
            ProductVariantDTO updatedVariant = productService.updateVariant(id, variantDTO);
            return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật biến thể thành công", updatedVariant));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi cập nhật biến thể: " + e.getMessage(), null));
        }
    }

    // Upload ảnh biến thể
    @PostMapping("/variant/images")
    public ResponseEntity<ResponseObject> uploadVariantImages(@RequestParam Long variantId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            // Kiểm tra xem có ảnh nào được gửi không
            if (images == null || images.length == 0) {
                return ResponseEntity.ok(new ResponseObject("error", "Không có ảnh nào được chọn", null));
            }

            List<ProductImageDTO> uploadedImages = productService.uploadVariantImages(variantId, images);
            return ResponseEntity.ok(new ResponseObject("ok", "Upload ảnh thành công", uploadedImages));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi upload ảnh: " + e.getMessage(), null));
        }
    }

    // Xóa ảnh
    @DeleteMapping("/image/{imageId}")
    public ResponseEntity<ResponseObject> deleteImage(@PathVariable Long imageId) {
        try {
            productService.deleteImage(imageId);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa ảnh thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi xóa ảnh: " + e.getMessage(), null));
        }
    }

    // Upload ảnh sản phẩm chính
    @PostMapping("/{productId}/images")
    public ResponseEntity<ResponseObject> uploadProductImages(@PathVariable Long productId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            // Kiểm tra xem có ảnh nào được gửi không
            if (images == null || images.length == 0) {
                return ResponseEntity.ok(new ResponseObject("error", "Không có ảnh nào được chọn", null));
            }

            List<ProductImageDTO> uploadedImages = productService.uploadProductImages(productId, images);
            return ResponseEntity.ok(new ResponseObject("ok", "Upload ảnh sản phẩm thành công", uploadedImages));
        } catch (Exception e) {
            return ResponseEntity
                    .ok(new ResponseObject("error", "Lỗi khi upload ảnh sản phẩm: " + e.getMessage(), null));
        }
    }

    // Xóa ảnh sản phẩm
    @DeleteMapping("/product/image/{imageId}")
    public ResponseEntity<ResponseObject> deleteProductImage(@PathVariable Long imageId) {
        try {
            productService.deleteProductImage(imageId);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa ảnh sản phẩm thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi xóa ảnh sản phẩm: " + e.getMessage(), null));
        }
    }

    // Lấy ảnh sản phẩm
    @GetMapping("/{productId}/images")
    public ResponseEntity<ResponseObject> getProductImages(@PathVariable Long productId) {
        try {
            List<ProductImageDTO> images = productService.getProductThumbnails(productId);
            return ResponseEntity.ok(new ResponseObject("ok", "Lấy ảnh sản phẩm thành công", images));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi lấy ảnh sản phẩm: " + e.getMessage(), null));
        }
    }

    // Tạo biến thể tự động
    @PostMapping("/{productId}/generate-variants")
    public ResponseEntity<ResponseObject> generateVariants(@PathVariable Long productId) {
        try {
            List<ProductVariantDTO> generatedVariants = productService.generateVariants(productId);
            return ResponseEntity.ok(new ResponseObject("ok", "Tạo biến thể thành công", generatedVariants));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi tạo biến thể: " + e.getMessage(), null));
        }
    }

    // Áp dụng hàng loạt - Chỉnh sửa giá
    @PutMapping("/variants/bulk-price")
    public ResponseEntity<ResponseObject> bulkUpdatePrice(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> variantIds = (List<Long>) request.get("variantIds");
            String priceStr = (String) request.get("price");

            Double price = priceStr != null && !priceStr.isEmpty() ? Double.parseDouble(priceStr) : null;

            productService.bulkUpdatePrice(variantIds, price);
            return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật giá hàng loạt thành công", null));
        } catch (Exception e) {
            return ResponseEntity
                    .ok(new ResponseObject("error", "Lỗi khi cập nhật giá hàng loạt: " + e.getMessage(), null));
        }
    }

    // Áp dụng hàng loạt - Chỉnh sửa số lượng
    @PutMapping("/variants/bulk-stock")
    public ResponseEntity<ResponseObject> bulkUpdateStock(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> variantIds = (List<Long>) request.get("variantIds");
            String stockStr = (String) request.get("stock");

            Integer stock = stockStr != null && !stockStr.isEmpty() ? Integer.parseInt(stockStr) : null;

            productService.bulkUpdateStock(variantIds, stock);
            return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật số lượng hàng loạt thành công", null));
        } catch (Exception e) {
            return ResponseEntity
                    .ok(new ResponseObject("error", "Lỗi khi cập nhật số lượng hàng loạt: " + e.getMessage(), null));
        }
    }

    // Áp dụng hàng loạt - Xóa biến thể
    @DeleteMapping("/variants/bulk-delete")
    public ResponseEntity<ResponseObject> bulkDeleteVariants(@RequestBody Map<String, Object> request) {
        try {
            List<Long> variantIds = ((List<?>) request.get("variantIds")).stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .collect(Collectors.toList());

            productService.bulkDeleteVariants(variantIds);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa biến thể hàng loạt thành công", null));
        } catch (Exception e) {
            return ResponseEntity
                    .ok(new ResponseObject("error", "Lỗi khi xóa biến thể hàng loạt: " + e.getMessage(), null));
        }
    }

    // Xóa biến thể đơn lẻ
    @DeleteMapping("/variant/{variantId}")
    public ResponseEntity<ResponseObject> deleteVariant(@PathVariable Long variantId) {
        try {
            productService.deleteVariant(variantId);
            return ResponseEntity.ok(new ResponseObject("ok", "Xóa biến thể thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi xóa biến thể: " + e.getMessage(), null));
        }
    }
}
