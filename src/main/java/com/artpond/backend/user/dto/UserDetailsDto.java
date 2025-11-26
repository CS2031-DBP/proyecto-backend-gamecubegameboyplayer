package com.artpond.backend.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailsDto extends PublicUserDto {
    private String description;
    
    private int followersCount;
    private int followingCount;
}
