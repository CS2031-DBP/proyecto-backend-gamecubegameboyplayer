package com.artpond.backend.publication.dto;

import com.artpond.backend.publication.domain.PubType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FailedAiTaskDto {
    private Long id;
    private Long publicationId;
    private PubType pubType;
    private String errorMessage;
    private LocalDateTime failedAt;
}
