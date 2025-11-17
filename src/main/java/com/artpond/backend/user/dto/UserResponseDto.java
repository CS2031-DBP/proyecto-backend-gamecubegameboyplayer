package com.artpond.backend.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponseDto extends PublicUserDto {
    private String email;
    private String description;
}
