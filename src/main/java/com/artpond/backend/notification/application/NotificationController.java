package com.artpond.backend.notification.application;

import com.artpond.backend.notification.domain.NotificationService;
import com.artpond.backend.notification.dto.NotificationDto;
import com.artpond.backend.user.domain.User;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(@AuthenticationPrincipal User user, Pageable pageable) {
        var notificationsPage = notificationService.getUserNotifications(user.getUserId(), pageable);

        Page<NotificationDto> dtoPage = notificationsPage
                .map(notification -> modelMapper.map(notification, NotificationDto.class));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getUserId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.markAsRead(id, user.getUserId());
        return ResponseEntity.ok().build();
    }
}