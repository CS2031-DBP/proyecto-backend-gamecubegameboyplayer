package com.artpond.backend.publication.application;

import com.artpond.backend.image.domain.Image;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/publication")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ARTIST')")
    public ResponseEntity<PublicationResponseDto> createPublication(@Valid @RequestBody CreatePublicationDto dto,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        PublicationResponseDto createdPublication =  publicationService.createPublication(dto, userDetails.getUsername());
        return ResponseEntity.created(URI.create("/publication/" + createdPublication.getId())).body(createdPublication);
    } /// hacer publication

    @GetMapping
    public ResponseEntity<Page<PublicationResponseDto>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(publicationService.getAllPublications(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponseDto> getPublication(@PathVariable Long id) {
        return ResponseEntity.ok(publicationService.getPublicationById(id));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<PublicationResponseDto> addImages(@PathVariable Long id,
                                       @RequestBody List<Image> images) {
        return ResponseEntity.ok(publicationService.saveImages(id, images));
    }

    ///  GET BY TAGS -> ALL

    ///  UPDATE (TAGS DESCRIPTION?)

    ///  DELETE
}
