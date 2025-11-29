package com.artpond.backend.publication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class UpdatePublicationDto {
    @Size(max = 256, message = "La descripción no puede exceder los 256 caracteres.")
    private String description;

    @Size(max = 30, message = "Maximo 30 etiquetas permitidas.")
    private List<@NotBlank @Size(min = 2, max = 30) String> tags;

    private Boolean contentWarning;
}
