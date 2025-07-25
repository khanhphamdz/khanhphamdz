package com.datn.teeshirt.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.datn.teeshirt.Entity.Payment.PaymentType;

public class PaymentDTO {
    private PaymentType paymentType; // COD, VNPAY
    private BigDecimal amount;
    private String paymentStatus; // pending, completed, failed, refunded
    private LocalDateTime paymentDate;
    private String transactionId;
    private String paymentDetails;
    // Getter & Setter
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
} 