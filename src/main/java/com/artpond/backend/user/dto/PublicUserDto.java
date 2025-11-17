package com.artpond.backend.user.dto;

import lombok.Data;

@Data
public class PublicUserDto {
    private Long userId;
    private String username;
    private String displayName;
    private String role;
}
