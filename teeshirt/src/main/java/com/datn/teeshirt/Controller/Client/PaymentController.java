package com.datn.teeshirt.Controller.Client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.datn.teeshirt.Config.VNPAYConfig;
import com.datn.teeshirt.DTO.PaymentResultDTO;
import com.datn.teeshirt.Entity.Payment;
import com.datn.teeshirt.Repository.OrderRepository;
import com.datn.teeshirt.Repository.PaymentRepository;
import com.datn.teeshirt.Service.EmailService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public PaymentController(PaymentRepository paymentRepository, OrderRepository orderRepository, EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    @GetMapping("/payment-result")
    public String viewPaymentResult(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
        Map fields = new HashMap<>();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName),
                    StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPAYConfig.hashAllFields(fields);
        PaymentResultDTO paymentResultDto = new PaymentResultDTO();
        paymentResultDto.setTxnRef(fields.get("vnp_TxnRef").toString());
        paymentResultDto.setAmount(String.valueOf(Double.parseDouble(fields.get("vnp_Amount").toString()) / 100));
        paymentResultDto.setBankCode(fields.get("vnp_BankCode").toString());
        paymentResultDto.setDatePay(fields.get("vnp_PayDate").toString());
        paymentResultDto.setResponseCode(fields.get("vnp_ResponseCode").toString());
        paymentResultDto.setTransactionStatus(fields.get("vnp_TransactionStatus").toString());

        boolean isSuccess = false;
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                isSuccess = true;
                // Cập nhật trạng thái payment và order
                String txnRef = fields.get("vnp_TxnRef").toString();
                java.util.List<Payment> payments = paymentRepository.findByTransactionId(txnRef);
                if (payments != null && !payments.isEmpty()) {
                    Payment payment = payments.get(0);
                    payment.setPaymentStatus("completed");
                    payment.setPaymentDate(LocalDateTime.now());
                    paymentRepository.save(payment);
                    // Cập nhật trạng thái đơn hàng nếu cần
                    if (payment.getOrder() != null) {
                        payment.getOrder().setStatus("paid");
                        orderRepository.save(payment.getOrder());
                        // Gửi email xác nhận đơn hàng khi thanh toán thành công
                        if (payment.getOrder().getCustomer() != null && payment.getOrder().getCustomer().getEmail() != null) {
                            String email = payment.getOrder().getCustomer().getEmail();
                            String subject = "Xác nhận đơn hàng #" + payment.getOrder().getOrderId();
                            StringBuilder body = new StringBuilder();
                            body.append("Cảm ơn bạn đã thanh toán thành công đơn hàng tại TeeShirtVibe!\nMã đơn hàng: ")
                                .append(payment.getOrder().getOrderId());
                            // Có thể bổ sung thêm thông tin đơn hàng nếu muốn
                            emailService.sendSimpleEmail(email, subject, body.toString());
                        }
                    }
                }
            }
        }
        // Redirect về trang shopping-cart với query param báo thành công/thất bại
        if (isSuccess) {
            return "redirect:/shopping-cart?order=success";
        } else {
            return "redirect:/shopping-cart?order=fail";
        }
    }

    private void updatePaymentStatus(Payment payment) {
        payment.setPaymentStatus("completed");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}

