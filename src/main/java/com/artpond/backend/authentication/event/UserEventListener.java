package com.artpond.backend.authentication.event;

import com.artpond.backend.authentication.application.MailService;
import com.artpond.backend.notification.domain.NotificationService;
import com.artpond.backend.notification.domain.NotificationType;
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) throws MessagingException {
        mailService.welcomeMail(event.getEmail(), event.getUsername(), event.getId());
        notificationService.createNotification(
                userRepository.findById(event.getId()).orElseThrow(() -> new UserNotFoundException()), null,
                NotificationType.WELCOME, event.getId(), "Te damos la bienvenida.");
    }

    @Async
    @EventListener
    public void handleUserModified(UserUpdatedEvent event) throws MessagingException {
        mailService.userChangedMail(event.getEmail(), event.getUsername());
    }
}
