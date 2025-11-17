package com.artpond.backend.publication.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "failed_place_tasks")
@Data
public class FailedPlaceTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long publicationId;
    private Long osmId;
    private String osmType;
    private String errorMessage;
    private LocalDateTime failedAt;
}
