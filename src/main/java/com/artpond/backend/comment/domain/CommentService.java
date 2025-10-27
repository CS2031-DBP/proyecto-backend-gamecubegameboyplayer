package com.artpond.backend.comment.domain;

import com.artpond.backend.comment.dto.CreateCommentDto;
import com.artpond.backend.comment.infrastructure.CommentRepository;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PublicationService publicationService;
    private final UserService userService;

    public Comment createComment(Long publicationId, CreateCommentDto dto, long userId) {
        // is a comment dto needed???
        Publication publication = publicationService.findPublicationById(publicationId);
        User author = userService.getUserById(userId);

        Comment comment = new Comment();
        comment.setContent(dto.getText());
        comment.setPublication(publication);
        comment.setUser(author);

        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long publicationId) {
        return commentRepository.findByPublicationId(publicationId);
    }
}
