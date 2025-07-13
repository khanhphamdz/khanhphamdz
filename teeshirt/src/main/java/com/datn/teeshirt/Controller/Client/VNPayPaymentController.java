package com.datn.teeshirt.Controller.Client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VNPayPaymentController {
    @Value("${vnpay.tmnCode:WSKPMBP7}")
    private String vnp_TmnCode;
    @Value("${vnpay.hashSecret:YXVDLHFPDPTUKXVYXDCAMLIPMBQOXWUV}")
    private String vnp_HashSecret;
    @Value("${vnpay.payUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_Url;
    @Value("${vnpay.returnUrl:http://localhost:8080/vnpay-return}")
    private String vnp_Returnurl;

    @PostMapping("/vnpay-payment")
    public ResponseEntity<?> createVnpayPayment(@RequestBody Map<String, Object> payload) {
        try {
            long amount = ((Number) payload.getOrDefault("amount", 0)).longValue() * 100; // VNPAY nhận đơn vị VND * 100
            String orderInfo = (String) payload.getOrDefault("orderInfo", "Thanh toán đơn hàng TeeShirtVibe");
            String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
            String vnp_IpAddr = "127.0.0.1";
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_BankCode", "NCB");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang" + orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl); // Đúng tên tham số
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            for (String fieldName : fieldNames) {
                String value = vnp_Params.get(fieldName);
                if (hashData.length() > 0)
                    hashData.append('&');
                hashData.append(fieldName).append('=').append(value);
                if (query.length() > 0)
                    query.append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            }

            String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            String paymentUrl = vnp_Url + "?" + query + "&vnp_SecureHash=" + secureHash;
            Map<String, String> resp = new HashMap<>();
            resp.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Lỗi tạo link thanh toán: " + e.getMessage()));
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
