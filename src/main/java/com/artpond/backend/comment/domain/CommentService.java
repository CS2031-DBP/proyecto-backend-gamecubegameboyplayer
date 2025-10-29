package com.artpond.backend.comment.domain;

import com.artpond.backend.comment.dto.CommentRequestDto;
import com.artpond.backend.comment.dto.CommentResponseDto;
import com.artpond.backend.comment.infrastructure.CommentRepository;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PublicationService publicationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public CommentResponseDto createComment(Long publicationId, CommentRequestDto dto, long userId) {
        Publication publication = publicationService.findPublicationById(publicationId);
        User author = userService.getUserById(userId);

        Comment comment = new Comment();
        comment.setContent(dto.getText());
        comment.setPublication(publication);
        comment.setUser(author);
        commentRepository.save(comment);

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setText(comment.getContent());
        commentResponseDto.setAuthor(modelMapper.map(author, UserResponseDto.class));
        commentResponseDto.setCreatedAt(comment.getCreatedDate());
        return commentResponseDto;
    }

    public List<CommentResponseDto> getComments(Long publicationId) {
        return commentRepository.findByPublicationId(publicationId).stream().map(comment -> {
            CommentResponseDto commentResponseDto = new CommentResponseDto();
            commentResponseDto.setId(comment.getId());
            commentResponseDto.setText(comment.getContent());
            commentResponseDto.setAuthor(modelMapper.map(comment.getUser(), UserResponseDto.class));
            commentResponseDto.setCreatedAt(comment.getCreatedDate());
            return commentResponseDto;
        }).toList();
    }

    public Void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (comment.getUser().getUserId().equals(userId)) {
            commentRepository.deleteById(commentId);
        }
        return null;
    }
}
