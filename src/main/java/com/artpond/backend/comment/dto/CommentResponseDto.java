package com.artpond.backend.comment.dto;

import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.dto.UserResponseDto;
import lombok.Data;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private UserResponseDto author;
    private Long publicationId;
}
