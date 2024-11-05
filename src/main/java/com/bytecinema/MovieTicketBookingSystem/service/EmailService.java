package com.bytecinema.MovieTicketBookingSystem.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
   private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;


    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();;
            e.getMessage();
            throw new RuntimeException();
        }
    }

    public void sendEmail2(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Tạo nội dung email từ template HTML
            String body = templateEngine.process(templateName, context);
            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void sendEmail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);

            // Tạo nội dung email từ template HTML
            String body = templateEngine.process(templateName, context);
            helper.setText(body, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String loadCssFromFile() throws IOException {
        Resource resource = new ClassPathResource("static/css/styles.css");
        String cssContent;
        try (InputStream inputStream = resource.getInputStream()) {
            cssContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        return cssContent ;
    }

}