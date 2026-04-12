package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public boolean sendEmailWithObject(String receivedEmail, Object object, String subject) {
        log.info("Sending email to: {}", receivedEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(receivedEmail);
            helper.setSubject(subject);
            helper.setText((String) object, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email: {}", e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi gửi email", e);
        }
        log.info("Send otp code to email successfully");
        return true;
    }
}
