package com.artpond.backend.publication.dto;

import lombok.Data;

@Data
public class PublicationCreatedDto {
    private Long id;
    private String url;
    private String description;
}