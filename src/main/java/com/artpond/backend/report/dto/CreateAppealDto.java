package com.artpond.backend.report.dto;

import lombok.Data;

@Data
public class CreateAppealDto {
    private Long publicationId;
    private String justification;
}
