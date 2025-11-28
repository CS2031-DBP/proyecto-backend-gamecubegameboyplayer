package com.artpond.backend.report.dto;

import java.time.LocalDateTime;

import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.report.domain.ReportReason;
import com.artpond.backend.report.domain.ReportStatus;
import com.artpond.backend.user.dto.PublicUserDto;

import lombok.Data;

@Data
public class ReportDto {
    private PublicUserDto reporter;
    private PublicationResponseDto publication;
    private PublicUserDto reportedUser;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private String resolutionNotes;
}
