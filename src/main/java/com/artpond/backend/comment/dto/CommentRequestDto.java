package com.artpond.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "El comentario no puede estar vacío.")
    @Size(max = 1024, message = "El comentario no puede exceder los 1024 caracteres.")
    private String text;
    
    private Long parentId;
}
