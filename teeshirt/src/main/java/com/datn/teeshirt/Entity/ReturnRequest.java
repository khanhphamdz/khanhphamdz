package com.datn.teeshirt.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

@Entity
@Table(name = "ReturnRequest")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "return_reason", nullable = false, length = 1000)
    private String returnReason;

    @Default
    @Column(name = "return_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReturnStatus returnStatus = ReturnStatus.PENDING;

    @Column(name = "return_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReturnType returnType;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_status", length = 20)
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_variant_id")
    private ProductVariant exchangeVariant;

    @Column(name = "return_quantity", nullable = false)
    private Integer returnQuantity;

    @Column(name = "return_images", columnDefinition = "NVARCHAR(MAX)")
    private String returnImages;

    @Column(name = "return_note", length = 1000)
    private String returnNote;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Employee processedBy;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnRequestItem> returnRequestItems;

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED
    }

    public enum ReturnType {
        REFUND, EXCHANGE
    }

    public enum RefundStatus {
        PENDING, COMPLETED, FAILED
    }
} 