package com.artpond.backend.user.domain;

import com.artpond.backend.authentication.event.UserUpdatedEvent;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.exception.PublicationNotFoundException;
import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

    @Autowired
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
        return user;
    }

    public UserResponseDto registerUser (RegisterUserDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setDisplayName(dto.getDisplayName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        return modelMapper.map( userRepository.saveAndFlush(user), UserResponseDto.class); // change to return user
    }

    public UserResponseDto patchUser(Long id, Map<String, Object> updates, Long userId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
        User reqUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        
        if (reqUser.getUserId() != user.getUserId())
            throw new ForbiddenException("No puedes editar el perfil de otra persona.");
            
        updates.forEach((key, value) -> {
            switch (key) {
                case "description" -> user.setDescription((@Size(min=0, max=256, message="La descripccion no debe de contener más de 256 caracteres.") String) value);
                case "email" -> changeEmail(user, (String) value);
                case "showExplicit" -> user.setShowExplicit(true);
            }
        });

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    private void changeEmail(User user, @Email String newMail) {
        String pastMail = user.getEmail();
        if (pastMail != newMail) {
            user.setEmail(newMail);
            eventPublisher.publishEvent(new UserUpdatedEvent(pastMail, user.getUsername()));
        }
    }

    public User getUserById (Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User findByEmail (String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User getUserByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if  (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No authenticated user found");
        }

        return (User)authentication.getPrincipal();
    }

    public Void deleteUserById (Long id) {
        userRepository.deleteById(id);
        return null;
    }
}