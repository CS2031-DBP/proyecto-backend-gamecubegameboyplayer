package com.artpond.backend.notification.domain;

import com.artpond.backend.notification.infrastructure.NotificationRepository;
import com.artpond.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void createNotification(User recipient, User actor, NotificationType type, Long refId, String message) {
        // No notificar si el usuario interactúa consigo mismo
        if (actor != null && recipient.getUserId().equals(actor.getUserId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setReferenceId(refId);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        if (n.getRecipient().getUserId().equals(userId)) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
    
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_UserIdAndIsReadFalse(userId);
    }
}
