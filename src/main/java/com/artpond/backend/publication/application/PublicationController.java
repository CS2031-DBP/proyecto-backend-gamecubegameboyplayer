package com.artpond.backend.publication.application;

import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.PublicationCreatedDto;
import com.artpond.backend.publication.dto.PublicationRequestDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/publication")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<PublicationCreatedDto> createPublication(
            @RequestPart("data") @Valid PublicationRequestDto dto,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal User userDetails) {
        
        PublicationCreatedDto createdPublication = publicationService.createPublication(dto, images, userDetails.getUsername());
        return ResponseEntity.created(URI.create("/publication/" + createdPublication.getId())).body(createdPublication);
    }

    @GetMapping
    public ResponseEntity<Page<PublicationResponseDto>> getAllPosts(Pageable pageable, @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(publicationService.getAllPublications(pageable, userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponseDto> getPublication(@PathVariable Long id, @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(publicationService.getPublicationById(id, userDetails));
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<Page<PublicationResponseDto>> getPublicationsByTag(@PathVariable String tagName,
                                                                             Pageable pageable, @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(publicationService.getPublicationsByTag(tagName, pageable, userDetails));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<PublicationResponseDto> patchPublication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(publicationService.patchPublication(id, updates, userDetails.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePublicationById(@PathVariable Long id,
                                                   @AuthenticationPrincipal User userDetails) {
        publicationService.deletePublicationById(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}