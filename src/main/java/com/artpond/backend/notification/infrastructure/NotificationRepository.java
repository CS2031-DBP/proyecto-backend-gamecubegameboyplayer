package com.artpond.backend.notification.infrastructure;

import com.artpond.backend.notification.domain.Notification;
import com.artpond.backend.notification.domain.NotificationType;
import com.artpond.backend.user.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipient_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByRecipient_UserIdAndIsReadFalse(Long userId);
    Boolean existsByRecipientAndActorAndType(User recipient, User actor, NotificationType type);
}
