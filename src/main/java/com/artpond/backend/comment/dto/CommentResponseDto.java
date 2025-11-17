package com.artpond.backend.comment.dto;

import com.artpond.backend.user.dto.PublicUserDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private PublicUserDto author;
    private LocalDateTime createdAt;
}
