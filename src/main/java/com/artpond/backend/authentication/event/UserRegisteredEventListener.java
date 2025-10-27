package com.artpond.backend.authentication.event;

import com.artpond.backend.authentication.application.MailService;
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
    public void handleUserRegistered(UserRegisteredEvent event) {
        mailService.welcomeMail(event.getEmail(), event.getUsername());
    }
}
