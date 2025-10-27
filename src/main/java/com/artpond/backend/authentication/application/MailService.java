package com.artpond.backend.authentication.application;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Async
    public void welcomeMail(String to, String username ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("bienvenid@ a artpond");
        message.setText("estamos felices de que seas parte de artpond, " + username);
        mailSender.send(message);
        System.out.println("mail to " + username);
    }
}