package com.artpond.backend.authentication.domain;

import com.artpond.backend.authentication.dto.JwtAuthLoginDto;
import com.artpond.backend.authentication.dto.LoginResponseDto;
import com.artpond.backend.authentication.event.UserRegisteredEvent;
import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.jwt.domain.JwtService;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
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
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher publisher;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto jwtRegister(final RegisterUserDto dto) throws BadCredentialsException {
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
}