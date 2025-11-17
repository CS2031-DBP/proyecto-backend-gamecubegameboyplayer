package com.artpond.backend.user.application;

import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.PublicUserDto;
import com.artpond.backend.user.dto.UserDetailsDto;
import com.artpond.backend.user.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/i/{id}")
    public ResponseEntity<UserDetailsDto> getUsers(@PathVariable Long id) {
        return ResponseEntity.ok(modelMapper.map(userService.getUserById(id), UserDetailsDto.class));
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserDto> getMethodName(@PathVariable String username) {
        return ResponseEntity.ok(modelMapper.map(userService.getUserByUsername(username), UserDetailsDto.class));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> putMethodName(@PathVariable Long id, Map<String, Object> updates, @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(userService.patchUser(id, updates, userDetails.getUserId()));
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
