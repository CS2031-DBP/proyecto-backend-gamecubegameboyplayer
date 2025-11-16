package com.artpond.backend.authentication.event;

import com.artpond.backend.authentication.application.MailService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {
    private final MailService mailService;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) throws MessagingException {
        mailService.welcomeMail(event.getEmail(), event.getUsername(), event.getId());
    }
}
