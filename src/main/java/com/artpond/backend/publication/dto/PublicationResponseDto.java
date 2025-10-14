package com.artpond.backend.publication.dto;

import com.artpond.backend.publication.domain.Publication;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicationResponseDto {
    private Long id;
    private String description;
    private Long userId;
    private LocalDateTime createdAt;
}
