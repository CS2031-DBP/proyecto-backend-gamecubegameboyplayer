package com.artpond.backend.notification.dto;

import java.time.LocalDateTime;
import com.artpond.backend.user.dto.PublicUserDto;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private PublicUserDto actor;
    private String type;
    private Long referenceId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
