package com.artpond.backend.user.application;

import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.PublicUserDto;
import com.artpond.backend.user.dto.UpdateUserDto;
import com.artpond.backend.user.dto.UserDetailsDto;
import com.artpond.backend.user.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/i/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsDto> getUsers(@PathVariable Long id) {
        return ResponseEntity.ok(modelMapper.map(userService.getUserById(id), UserDetailsDto.class));
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserDto> getMethodName(@PathVariable String username) {
        return ResponseEntity.ok(modelMapper.map(userService.getUserByUsername(username), UserDetailsDto.class));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> patchUser(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) @Valid UpdateUserDto dto, // Usamos DTO y @Valid
            @RequestPart(value = "watermark", required = false) MultipartFile watermark,
            @AuthenticationPrincipal User userDetails) {

        return ResponseEntity.ok(userService.patchUser(id, dto, watermark, userDetails.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User userDetails) {
        if (!userDetails.getUserId().equals(id)
                && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/switch-role")
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST')")
    public ResponseEntity<UserResponseDto> switchRole(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(userService.switchUserRole(userDetails.getUserId()));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> uploadAvatar(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(userService.updateAvatar(userDetails.getUserId(), image));
    }

    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleFollow(@PathVariable Long id, @AuthenticationPrincipal User userDetails) {
        userService.toggleFollow(userDetails.getUserId(), id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/save/{publicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleSavePost(@PathVariable Long publicationId, @AuthenticationPrincipal User userDetails) {
        userService.toggleSavePublication(userDetails.getUserId(), publicationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PublicationResponseDto>> getSavedPosts(
            @AuthenticationPrincipal User userDetails, 
            Pageable pageable) {
        return ResponseEntity.ok(userService.getSavedPublications(userDetails.getUserId(), pageable));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Page<PublicUserDto>> getFollowing(
            @PathVariable Long id, 
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUserFollowing(id, pageable));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<Page<PublicUserDto>> getFollowers(
            @PathVariable Long id, 
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUserFollowers(id, pageable));
    }
}
