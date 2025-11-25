package com.artpond.backend.comment.domain;

import com.artpond.backend.comment.dto.CommentRequestDto;
import com.artpond.backend.comment.dto.CommentResponseDto;
import com.artpond.backend.comment.event.CommentCreatedEvent;
import com.artpond.backend.comment.infrastructure.CommentRepository;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.user.domain.Role;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.PublicUserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PublicationService publicationService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponseDto createComment(Long publicationId, CommentRequestDto dto, long userId) {
        Publication publication = publicationService.findPublicationById(publicationId);
        User author = userService.getUserById(userId);

        Comment comment = new Comment();
        comment.setContent(dto.getText());
        comment.setPublication(publication);
        comment.setUser(author);

        if (dto.getParentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Comentario padre no encontrado"));
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);
        eventPublisher.publishEvent(new CommentCreatedEvent(this, savedComment));

        return mapToDto(savedComment);
    }

    public List<CommentResponseDto> getComments(Long publicationId) {
        List<Comment> rootComments = commentRepository.findByPublicationIdAndParentIsNull(publicationId);
        return rootComments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private CommentResponseDto mapToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getContent());
        dto.setAuthor(modelMapper.map(comment.getUser(), PublicUserDto.class));
        dto.setCreatedAt(comment.getCreatedDate());
        
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream().map(this::mapToDto).collect(Collectors.toList()));
        }
        return dto;
    }
    
    public Void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comentario no encontrado"));
        User requestUser = userService.getUserById(userId);

        boolean isAuthor = comment.getUser().getUserId().equals(userId);
        boolean isAdmin = requestUser.getRole() == Role.ADMIN || requestUser.getRole() == Role.MODERATOR;

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException("No tienes permiso para eliminar este comentario.");
        }
        
        commentRepository.deleteById(commentId);
        return null;
    }
}
