package com.datn.teeshirt.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDTO {
    private Long returnId;
    private Long orderId;
    private Long customerId;
    private String returnReason;
    private String returnStatus;
    private String returnType;
    private java.math.BigDecimal refundAmount;
    private String refundStatus;
    private Integer returnQuantity;
    private String requestDate;
    private String returnNote;
    private String returnImages;
    private java.util.List<ReturnRequestItemDTO> returnItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnRequestItemDTO {
        private Long variantId;
        private Integer returnQuantity;
        private String itemReturnReason;
        private String productCondition; // Tình trạng sản phẩm khi trả hàng
    }
}
