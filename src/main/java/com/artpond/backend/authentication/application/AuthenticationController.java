package com.artpond.backend.authentication.application;

import com.artpond.backend.authentication.domain.AuthenticationService;
import com.artpond.backend.authentication.domain.RefreshToken;
import com.artpond.backend.authentication.domain.RefreshTokenService;
import com.artpond.backend.authentication.dto.ForgotPasswordRequestDto;
import com.artpond.backend.authentication.dto.JwtAuthLoginDto;
import com.artpond.backend.authentication.dto.LoginResponseDto;
import com.artpond.backend.authentication.dto.ResetPasswordRequestDto;
import com.artpond.backend.user.dto.RegisterUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Value("${jwt.expiration-refresh}")
    private Long refreshTokenDurationMs;

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody final RegisterUserDto dto) {
        LoginResponseDto created = authenticationService.jwtRegister(dto);
        ResponseCookie cookie = createRefreshTokenCookie(created.getUserId());
        URI location = URI.create("/auth/user/" + created.getUserId());
        return ResponseEntity.created(location)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody final JwtAuthLoginDto dto) {
        LoginResponseDto loginResponse = authenticationService.jwtLogin(dto);
        ResponseCookie cookie = createRefreshTokenCookie(loginResponse.getUserId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        if (refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            authenticationService.logout(refreshToken);
        }
        
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(Long userId) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);
        long maxAgeSeconds = refreshTokenDurationMs / 1000;

        return ResponseCookie.from("refresh_token", refreshToken.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto dto) {
        authenticationService.forgotPassword(dto.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto dto) {
        authenticationService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }
}