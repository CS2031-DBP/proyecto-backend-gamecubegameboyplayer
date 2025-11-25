package com.artpond.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Size(max=256, message="La descripción no debe contener más de 256 caracteres.")
    private String description;

    @Email(message = "El formato del correo es inválido")
    private String email;

    private Boolean showExplicit;
}
