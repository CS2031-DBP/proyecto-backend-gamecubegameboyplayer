package com.artpond.backend.user.domain;

import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
        return modelMapper.map(user, UserDetails.class);
    }

    public UserResponseDto registerUser (RegisterUserDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setDisplayName("placeholder"); // display name dto
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // HASH HERE
        user.setRole(Role.USER);

        return modelMapper.map( userRepository.saveAndFlush(user), UserResponseDto.class); // change to return user
    }

    public User getUserById (Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User findByEmail (String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if  (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("no authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    public Void deleteUserById (Long id) {
        userRepository.deleteById(id);
        return null;
    }
/*
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow();
    }

 */
}