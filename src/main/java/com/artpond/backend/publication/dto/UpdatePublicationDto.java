package com.artpond.backend.publication.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class UpdatePublicationDto {
    @Size(max = 256, message = "La descripción no puede exceder los 256 caracteres.")
    private String description;

    @Size(max = 30, message = "Máximo 30 etiquetas permitidas.")
    private List<String> tags;

    private Boolean contentWarning;
}
