package com.artpond.backend.authentication.application;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${deployment.frontend.url}")
    private String frontendUrl;

    private void sendHTMLMail(final String to, final String subject, final String templateName,
            final Context context)
            throws MessagingException {
        final String emailContent = templateEngine.process(templateName, context);

        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }

    @Async
    public void welcomeMail(String send, String username, Long id) throws MessagingException {
        final Context context = new Context();
        context.setVariable("userName", username);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("generatedId", id);

        sendHTMLMail(send, "Artpond te da la bienvenida", "welcome", context);
    }

    @Async
    public void userChangedMail(String send, String username) throws MessagingException {
        final Context context = new Context();
        context.setVariable("userName", username);
        context.setVariable("frontendUrl", frontendUrl);

        sendHTMLMail(send, "Artpond: Aviso de cambio de correo electronico", "emailchange", context);
    }

    @Async
    public void sendPasswordResetMail(String to, String username, String token) throws MessagingException {
        Context context = new Context();
        context.setVariable("username", username);
        // Asumiendo que tu frontend tiene una ruta /reset-password?token=...
        context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + token);

        sendHTMLMail(to, "Artpond: Recuperación de contraseña", "reset-password", context);
    }
}