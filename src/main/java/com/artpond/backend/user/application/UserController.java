package com.artpond.backend.user.application;

import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User createdUser = userService.registerUser(user);
        URI location = URI.create("/users/" + createdUser.getId());
        return ResponseEntity.created(location).body(createdUser);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUsers(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
