package com.artpond.backend.comment.application;

import com.artpond.backend.comment.domain.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    @PostMapping
    public ResponseEntity<?> publishComment() {
        return ResponseEntity.ok().build();
    }
}
