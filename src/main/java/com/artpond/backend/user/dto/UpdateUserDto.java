package com.artpond.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Size(max=256, message="La descripción no debe contener más de 256 caracteres.")
    private String description;

    @Email(message = "El formato del correo es inválido")
    private String email;

    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username solo puede contener letras, números, _ y -")
    private String username;

    @Size(max=50, message="El display name no debe contener más de 256 caracteres.")
    private String displayName;

    private Boolean showExplicit;
}
