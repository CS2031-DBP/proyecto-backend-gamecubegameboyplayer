package com.artpond.backend.report.dto;

import java.time.LocalDateTime;

import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.report.domain.AppealStatus;
import com.artpond.backend.user.dto.PublicUserDto;

import lombok.Data;

@Data
public class AppealDto {
    private PublicUserDto author;
    private PublicationResponseDto publication;
    private String justification;
    private AppealStatus status;
    private LocalDateTime createdAt;
    private String adminNotes;
}
