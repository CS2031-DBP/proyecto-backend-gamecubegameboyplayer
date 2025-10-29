package com.artpond.backend.user.application;

import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.PublicUserDto;

import com.artpond.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUsers(@PathVariable Long id) {
        return ResponseEntity.ok(modelMapper.map(userService.getUserById(id), UserResponseDto.class));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
