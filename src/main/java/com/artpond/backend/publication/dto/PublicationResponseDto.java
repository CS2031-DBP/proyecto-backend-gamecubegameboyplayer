package com.artpond.backend.publication.dto;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.tag.domain.Tag;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.dto.UserResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicationResponseDto {
    private Long id;
    private String description;
    private UserResponseDto author;
    private List<String> tags;
}
