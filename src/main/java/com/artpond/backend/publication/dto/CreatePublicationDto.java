package com.artpond.backend.publication.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePublicationDto {
    @Size(max = 255)
    private String description;
    @NotNull
    private Boolean contentWarning;
    @Size(max = 15)
    private List<String> tags = new ArrayList<>();
    // add image
}
