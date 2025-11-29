package com.artpond.backend.authentication.domain;

import com.artpond.backend.authentication.application.MailService;
import com.artpond.backend.authentication.dto.JwtAuthLoginDto;
import com.artpond.backend.authentication.dto.LoginResponseDto;
import com.artpond.backend.authentication.event.UserRegisteredEvent;
import com.artpond.backend.authentication.infrastructure.PasswordResetTokenRepository;
import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.jwt.domain.JwtService;
import com.artpond.backend.user.domain.AuthProvider;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import com.artpond.backend.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher publisher;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto jwtRegister(final RegisterUserDto dto) throws BadRequestException {
        final UserResponseDto createdUser = userService.registerUser(dto, passwordEncoder);

        final UserDetails user = userService.loadUserByUsername(createdUser.getUsername());
        final String token = jwtService.generateToken(user);

        LoginResponseDto response = modelMapper.map(createdUser, LoginResponseDto.class);
        response.setToken(token);

        publisher.publishEvent(new UserRegisteredEvent(createdUser.getUserId(), createdUser.getEmail(), createdUser.getUsername()));
        return response;
    }

    public LoginResponseDto jwtLogin(final JwtAuthLoginDto dto) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);

        LoginResponseDto response = modelMapper.map(user, LoginResponseDto.class);
        response.setToken(accessToken);
        return response;
    }

    @Transactional
    public LoginResponseDto refreshToken(String requestRefreshToken) throws BadCredentialsException {
        RefreshToken token = refreshTokenService.findByToken(requestRefreshToken);
        token = refreshTokenService.verifyExpiration(token);

        User user = token.getUser();
        String accessToken = jwtService.generateToken(user);

        LoginResponseDto response = modelMapper.map(user, LoginResponseDto.class);
        response.setToken(accessToken);
        return response;
    }

    public void logout(String refreshToken) throws BadCredentialsException {
        refreshTokenService.deleteByToken(refreshToken);
    }

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService; // Asegúrate de tenerlo inyectado

    @Transactional
    public void forgotPassword(String email) {
        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return; 
        }
        User user = userOptional.get();

        if (AuthProvider.GOOGLE.equals(user.getProvider())) {
            return; 
        }

        passwordResetTokenRepository.deleteByUser_UserId(user.getUserId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(resetToken);

        try {
            mailService.sendPasswordResetMail(user.getEmail(), user.getUsername(), token);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo de recuperación");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("El token ha expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}