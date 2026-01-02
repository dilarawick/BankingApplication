package com.bankapp.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(
            String email,
            String name,
            String otp
    ) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("OTP for Nova Banking");
            helper.setFrom("novabanking.noreply@gmail.com");

            ClassPathResource htmlFile =
                    new ClassPathResource("static/common/password-reset.html");

            String html = new String(
                    htmlFile.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            html = html.replace("{{NAME}}", name);
            html = html.replace("{{OTP}}", otp);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send otp", e);
        }
    }
}
