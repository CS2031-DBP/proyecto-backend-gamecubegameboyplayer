package com.artpond.backend.publication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PublicationRequestDto {
    @Size(min=0, max=256, message="La publicacion no debe de contener más de 256 caracteres.")
    private String description;

    @NotNull
    private Boolean contentWarning;
    @NotNull
    private Boolean machineGenerated; // No incluira el tag de ia, pero el sistema igualmente va a pasar por una cola de revisión.

    @Size(min=0, max=30, message="No puedes tener más de 30 etiquetas.")
    private List<String> tags;

    private Long osmId; // ubicacion mundial del lugar este
    private String osmType; // "node" o "way" o "relation" segun el identificador
    private Boolean hideCleanImage = false;
}
