package com.artpond.backend.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponseDto extends UserDetailsDto {
    private String email;
    private Boolean showExplicit;
}
