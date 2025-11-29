package com.artpond.backend.publication.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FailedPlaceTaskDto {
    private Long id;
    private Long publicationId;
    private Long osmId;
    private String osmType;
    private String errorMessage;
    private LocalDateTime failedAt;
}
