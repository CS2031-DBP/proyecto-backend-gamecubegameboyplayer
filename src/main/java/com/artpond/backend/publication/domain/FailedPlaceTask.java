package com.artpond.backend.publication.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
    
    @Column
    private Long publicationId;
    @Column
    private Long osmId;
    @Column
    private String osmType;
    @Column
    private String errorMessage;
    @Column
    private LocalDateTime failedAt;
}
