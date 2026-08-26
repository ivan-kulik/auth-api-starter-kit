package com.starter.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = this.mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(
                    message, false, StandardCharsets.UTF_8.name()
            );

            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);

            messageHelper.setText(htmlBody, true);

            this.mailSender.send(message);
        } catch (MessagingException exception) {
            // TODO: send event to DLQ
            throw new IllegalStateException("Failed to send email to: " + to, exception);
        }
    }
}
