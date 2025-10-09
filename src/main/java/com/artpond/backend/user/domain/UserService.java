package com.artpond.backend.user.domain;

import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserDto;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserDto registerUser (RegisterUserDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setDisplayName("dto.getDisplayName()");
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // HASH HERE
        user.setRole(Role.USER);

        return modelMapper.map( userRepository.save(user), UserDto.class);
    }

    public User getUserById (Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User findByEmail (String email) {
        return userRepository.findByEmail(email);
    }
}