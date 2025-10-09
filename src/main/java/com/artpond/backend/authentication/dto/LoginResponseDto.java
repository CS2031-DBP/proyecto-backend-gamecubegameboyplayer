package com.artpond.backend.authentication.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String email;
    private String role;
}
