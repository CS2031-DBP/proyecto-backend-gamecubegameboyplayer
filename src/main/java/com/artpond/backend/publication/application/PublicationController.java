package com.artpond.backend.publication.application;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.dto.UpdatePublicationDto;
import com.artpond.backend.publication.exception.PublicationNotFoundException;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/publication")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;
    // TODO add the comment service around these lines
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_USER')")
    public PublicationResponseDto createPost(@Valid @ModelAttribute final CreatePublicationDto dto,
            final Authentication authentication)
            throws IOException {
        final User user = (User) authentication.getPrincipal();
        //userService.checkUserVerified(user);

        return publicationService.createPublication(user, dto);
    }

    @GetMapping
    public ResponseEntity<List<PublicationResponseDto>> getAllPosts(Authentication authentication) {
        return ResponseEntity.ok(publicationService.getAllPublications());
    }

    ///  GET SINGULAR PUBLICATION
    @GetMapping("/{id}")
    public PublicationResponseDto getIndividualPost(@PathVariable final Long id) {
        Publication post = publicationService.getPublicationFromId(id).orElseThrow(PublicationNotFoundException::new);

        PublicationResponseDto response = modelMapper.map(post, PublicationResponseDto.class);
        //response.setTotalComments(commentService.countPostComments(post.getId()));
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public PublicationResponseDto updatePost(@PathVariable final Long id, @Valid @RequestBody final UpdatePublicationDto dto,
            final Authentication authentication) {
        Publication post = publicationService.getPublicationFromId(id).orElseThrow(PublicationNotFoundException::new);
        User user = (User) authentication.getPrincipal();

        if (!user.equals(post.getOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid access to publication config.");
        }

        Publication updatedPost = publicationService.updatePublication(post, dto);
        return modelMapper.map(updatedPost, PublicationResponseDto.class);
    }

    ///  GET BY TAGS -> ALL

    ///  UPDATE (TAGS DESCRIPTION?)

    ///  DELETE
}
