package com.artpond.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserDto {
    private Long userId;

    @NotEmpty
    private String username;
    @Email
    private String email;
    @NotEmpty
    private String role;
}
