package com.artpond.backend.authentication.domain;

import com.artpond.backend.authentication.dto.JwtAuthLoginDto;
import com.artpond.backend.authentication.dto.LoginResponseDto;
import com.artpond.backend.authentication.event.UserRegisteredEvent;
import com.artpond.backend.jwt.domain.JwtService;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.RegisterUserDto;
import com.artpond.backend.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher publisher;

    public LoginResponseDto jwtRegister(final RegisterUserDto dto) {
        final UserResponseDto createdUser = userService.registerUser(dto, passwordEncoder);
        //userService.sendVerificationEmail(createdUser);

        final UserDetails userDetails = userService.loadUserByUsername(createdUser.getUsername());

        final String token = jwtService.generateToken(userDetails);

        LoginResponseDto response = modelMapper.map(createdUser, LoginResponseDto.class);
        response.setToken(token);

        publisher.publishEvent(new UserRegisteredEvent(createdUser.getId(), createdUser.getEmail(), createdUser.getUsername()));
        return response;
    }

    public LoginResponseDto jwtLogin(final JwtAuthLoginDto dto) {
        /*
        final User user = userService.findByEmail(dto.getEmail());

        if (user.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            return null;

        final String token = jwtService.generateToken(modelMapper.map(user, UserDto.class));
        return new LoginResponseDto(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );

         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        LoginResponseDto response = modelMapper.map(userDetails, LoginResponseDto.class);
        response.setToken(token);
        return response;
    }
}
