package com.artpond.backend.user.dto;

import java.util.List;

import com.artpond.backend.publication.dto.PublicationResponseDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailsDto extends PublicUserDto {
    private String description;
    private List<PublicationResponseDto> publications;
    
    private int followersCount;
    private int followingCount;
}
