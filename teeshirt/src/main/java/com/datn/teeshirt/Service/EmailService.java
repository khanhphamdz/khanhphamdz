package com.datn.teeshirt.Service;

import com.datn.teeshirt.Entity.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {
    @Autowired
    CustomerService customerRepository;
    @Autowired
    JavaMailSender mailSender;

    public boolean sendPasswordToEmail(String email) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // Gửi email chứa mật khẩu hiện tại
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Mật khẩu hiện tại của bạn");
            message.setText("Xin chào " + customer.getName() + ",\n\n" +
                    "Bạn vừa yêu cầu lấy lại mật khẩu cho tài khoản tại TeeShirt Vibe Shop.\n" +
                    "Mật khẩu hiện tại của bạn là: " + customer.getPassword() + "\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n" +
                    "\n---\n" +
                    "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của TeeShirt Vibe!\n" +
                    "Chúc bạn một ngày tốt lành!\n" +
                    "\nTeeShirt Vibe - Thời trang cho mọi cá tính.");
            mailSender.send(message);
            return true;
        }
        return false;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}