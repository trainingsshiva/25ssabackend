package com.candidate.management.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;

    public void sendEmailAlert(String to, String subject, String body) {
        log.info("--- [EMAIL ALERT TRIGGERED] ---");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("--------------------------------");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("alerts@candidate-management.com");
            mailSender.send(message);
            log.info("Email sent successfully using SMTP configuration.");
        } catch (Exception e) {
            log.warn("Failed to send email using SMTP (normal behavior if SMTP is not configured): {}. Alert remains visible on Dashboard.", e.getMessage());
        }
    }
}
