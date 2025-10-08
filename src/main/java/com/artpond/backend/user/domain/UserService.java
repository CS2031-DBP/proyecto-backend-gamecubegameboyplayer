package com.artpond.backend.user.domain;

import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser (User user) {
        return userRepository.save(user);
    }

    public User findById (Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User findByEmail (String email) {
        return userRepository.findByEmail(email);
    }
}