package com.example.Music_management.service.impl;

import com.example.Music_management.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender sender;

    /** 发件人 = SMTP 登录账号(多数邮件服务商要求两者一致,否则拒发) */
    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(from);
        sender.send(message);
    }
}
