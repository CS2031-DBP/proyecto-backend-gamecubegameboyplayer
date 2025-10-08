package com.artpond.backend.user.domain;

import com.artpond.backend.user.insfrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}
