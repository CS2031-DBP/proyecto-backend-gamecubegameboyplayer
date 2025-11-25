package com.artpond.backend.comment.dto;

import com.artpond.backend.user.dto.PublicUserDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private PublicUserDto author;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> replies; 
}
