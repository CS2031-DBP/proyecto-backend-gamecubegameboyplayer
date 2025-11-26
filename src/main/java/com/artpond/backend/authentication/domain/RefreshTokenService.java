package com.artpond.backend.authentication.domain;

import com.artpond.backend.authentication.infrastructure.RefreshTokenRepository;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;
import com.artpond.backend.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.expiration-refresh}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final int MAX_SESSIONS = 5;

    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).get();

        verifySessionLimit(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    private void verifySessionLimit(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserOrderByExpiryDateAsc(user);
        
        if (tokens.size() >= MAX_SESSIONS) {
            int tokensToDelete = tokens.size() - MAX_SESSIONS + 1;
            
            for (int i = 0; i < tokensToDelete; i++) {
                refreshTokenRepository.delete(tokens.get(i));
            }
        }
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new ForbiddenException("Refresh token was expired. Please make a new signin request.");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token is not in database."));
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}