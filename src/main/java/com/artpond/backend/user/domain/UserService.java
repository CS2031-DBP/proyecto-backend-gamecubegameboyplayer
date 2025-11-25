package com.artpond.backend.user.domain;

import com.artpond.backend.authentication.event.UserUpdatedEvent;
import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.image.domain.Image;
import com.artpond.backend.image.domain.ImageService; 
import com.artpond.backend.image.domain.WatermarkService;
import com.artpond.backend.notification.domain.NotificationService;
import com.artpond.backend.notification.domain.NotificationType;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import com.artpond.backend.user.dto.UpdateUserDto; 
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final WatermarkService watermarkService;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    private final NotificationService notificationService;
    private final PublicationRepository publicationRepository;

    @Transactional
    public UserResponseDto updateAvatar(Long userId, MultipartFile file) {
        User user = getUserById(userId);
        try {
            String avatarUrl = imageService.uploadAvatar(file, userId);
            user.setProfilePictureUrl(avatarUrl);
            return modelMapper.map(userRepository.save(user), UserResponseDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen de perfil", e);
        }
    }

    @Transactional
    public void toggleFollow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new BadRequestException("No puedes seguirte a ti mismo.");
        }

        User follower = getUserById(followerId);
        User followed = getUserById(followedId);

        if (follower.getFollowing().contains(followed)) {
            follower.getFollowing().remove(followed);
            followed.getFollowers().remove(follower);
        } else {
            follower.getFollowing().add(followed);
            followed.getFollowers().add(follower);

            notificationService.createNotification(
                followed,
                follower,
                NotificationType.WELCOME,
                follower.getUserId(),
                follower.getDisplayName() + " ha comenzado a seguirte."
            );
        }
        userRepository.save(follower);
        userRepository.save(followed);
    }
    
    public boolean isFollowing(Long followerId, Long followedId) {
        User follower = getUserById(followerId);
        return follower.getFollowing().stream().anyMatch(u -> u.getUserId().equals(followedId));
    }

    @Transactional
    public void toggleSavePublication(Long userId, Long publicationId) {
        User user = getUserById(userId);
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NotFoundException("Publicación no encontrada"));

        if (user.getSavedPublications().contains(publication)) {
            user.getSavedPublications().remove(publication);
        } else {
            user.getSavedPublications().add(publication);
        }
        userRepository.save(user);
    }
    
    public Page<PublicationResponseDto> getSavedPublications(Long userId, Pageable pageable) {
        return publicationRepository.findSavedPublicationsByUser(userId, pageable)
                .map(pub -> modelMapper.map(pub, PublicationResponseDto.class));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }

    public UserResponseDto registerUser (RegisterUserDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setDisplayName(dto.getDisplayName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        return modelMapper.map(userRepository.saveAndFlush(user), UserResponseDto.class);
    }

    @Transactional
    public UserResponseDto patchUser(Long id, UpdateUserDto dto, MultipartFile watermark, Long userId) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        
        if (!user.getUserId().equals(userId))
            throw new ForbiddenException("No puedes editar el perfil de otra persona.");
        
        if (watermark != null && !watermark.isEmpty()) {
            try {
                watermarkService.uploadWatermark(watermark, user.getUsername());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload watermark: " + e.getMessage());
            }
        }
            
        if (dto != null) {
            if (dto.getDescription() != null) user.setDescription(dto.getDescription());
            if (dto.getShowExplicit() != null) user.setShowExplicit(dto.getShowExplicit());
            if (dto.getEmail() != null) changeEmail(user, dto.getEmail());
        }

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    private void changeEmail(User user, @Email String newMail) {
        String pastMail = user.getEmail();
        if (!pastMail.equals(newMail)) {
            user.setEmail(newMail);
            eventPublisher.publishEvent(new UserUpdatedEvent(pastMail, user.getUsername()));
        }
    }

    public UserResponseDto switchUserRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Los administradores no pueden cambiar su rol manualmente.");
        }

        boolean hasPublications = !user.getPublications().isEmpty(); 
        if (!hasPublications) {
            throw new BadRequestException("Debes tener al menos una publicación para cambiar tu rol a ARTISTA.");
        }

        user.setRole(user.getRole() == Role.USER ? Role.ARTIST : Role.USER);
        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    public User getUserById (Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
    }

    public User findByEmail (String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("No existe usuario con este email."));
    }

    public User getUserByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(() -> new NotFoundException("No existe usuario con este nombre."));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if  (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("No authenticated user found.");
        }
        return (User)authentication.getPrincipal();
    }

    @Transactional
    public Void deleteUserById (Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        List<Publication> publications = user.getPublications();
        for (Publication pub : publications) {
            List<String> cleanKeys = pub.getImages().stream().map(Image::getCleanFileKey).collect(toList());
            List<String> publicKeys = pub.getImages().stream().map(Image::getPublicFileKey).collect(toList());
            imageService.deleteMultipleImages(cleanKeys, publicKeys);
        }

        watermarkService.deleteUserWatermark(user.getUsername());
        userRepository.deleteById(id);
        return null;
    }
}