package com.artpond.backend.user.application;

import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.PublicUserDto;
import com.artpond.backend.user.dto.UserDetailsDto;
import com.artpond.backend.user.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.PubType;
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

    @PatchMapping(value = "/{id}", consumes = PubType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> patchUser(
            @PathVariable Long id,
            @RequestPart(value = "data", required = false) Map<String, Object> updates,
            @RequestPart(value = "watermark", required = false) MultipartFile watermark,
            @AuthenticationPrincipal User userDetails) {
        
        return ResponseEntity.ok(userService.patchUser(id, updates, watermark, userDetails.getUserId()));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User userDetails) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/switch-role")
    @PreAuthorize("hasRole('USER') or hasRole('ARTIST')")
    public ResponseEntity<UserResponseDto> switchRole(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(userService.switchUserRole(userDetails.getUserId()));
    }
}
