package com.datn.teeshirt.DTO;

import lombok.Data;


@Data
public class PaymentResultDTO {
    private String txnRef;
    private String amount;
    private String bankCode;
    private String datePay;
    private String responseCode;
    private String transactionStatus;
}
