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
        commentResponseDto.setPublicationId(publicationId);
        return commentResponseDto;
    }

    public List<Comment> getComments(Long publicationId) {
        return commentRepository.findByPublicationId(publicationId);
    }
}
