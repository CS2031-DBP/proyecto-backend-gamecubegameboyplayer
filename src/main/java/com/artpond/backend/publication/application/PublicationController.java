package com.artpond.backend.publication.application;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/publication")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    @PostMapping
    public ResponseEntity<PublicationResponseDto> createPost(@Valid @RequestBody CreatePublicationDto dto,
                                                  Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PublicationResponseDto createdPublication = publicationService.createPublication(user, dto);
        return ResponseEntity.created(URI.create("/publication/" + createdPublication.getId())).body(createdPublication);
    } /// hacer publication

    @GetMapping
    public ResponseEntity<List<PublicationResponseDto>> getAllPosts(Authentication authentication) {
        return ResponseEntity.ok(publicationService.getAllPublications());
    }

    ///  GET SINGULAR PUBLICATION

    ///  GET BY TAGS -> ALL

    ///  UPDATE (TAGS DESCRIPTION?)

    ///  DELETE
}
