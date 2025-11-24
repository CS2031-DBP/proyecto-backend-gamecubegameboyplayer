package com.artpond.backend.config;

import com.artpond.backend.jwt.domain.JwtService;
import com.artpond.backend.authentication.domain.RefreshToken;
import com.artpond.backend.authentication.domain.RefreshTokenService;
import com.artpond.backend.user.domain.Role;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.infrastructure.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${deployment.frontend.url}") 
    private String frontendUrl;
    
    @Value("${jwt.expiration-refresh}")
    private Long refreshTokenDurationMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewGoogleUser(email, name));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken.getToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); 
        refreshCookie.setPath("/auth/refresh");
        refreshCookie.setMaxAge((int) (refreshTokenDurationMs / 1000));
        response.addCookie(refreshCookie);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User registerNewGoogleUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(name);
        user.setUsername(email.split("@")[0] + UUID.randomUUID().toString().substring(0, 5)); 
        user.setRole(Role.USER);
        user.setShowExplicit(false);
        user.setDescription("");

        String randomPassword = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));
        
        return userRepository.save(user);
    }
}