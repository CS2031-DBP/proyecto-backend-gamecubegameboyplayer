package com.artpond.backend.comment.application;

import com.artpond.backend.comment.domain.CommentService;
import com.artpond.backend.comment.dto.CommentRequestDto;
import com.artpond.backend.comment.dto.CommentResponseDto;
import com.artpond.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/publication/{publicationId}/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long publicationId,
                                                         @RequestBody CommentRequestDto dto,
                                                         @AuthenticationPrincipal User user) {
        CommentResponseDto comment = commentService.createComment(publicationId, dto, user.getUserId());
        return ResponseEntity.created(URI.create("/comment/" + comment.getId())).body(comment);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long publicationId) {
        return ResponseEntity.ok(commentService.getComments(publicationId));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @AuthenticationPrincipal User user) {
        commentService.deleteComment(commentId, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
