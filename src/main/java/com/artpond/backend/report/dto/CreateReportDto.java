package com.artpond.backend.report.dto;

import com.artpond.backend.report.domain.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportDto {
    private Long publicationId;
    private Long reportedUserId;
    
    @NotNull
    private ReportReason reason;
    private String details;
}
