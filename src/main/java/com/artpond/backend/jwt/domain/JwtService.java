package com.artpond.backend.jwt.domain;

import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String key;

    private final UserService userService;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    public <T> T extractClaim(Claims claims, Function<Claims, T> resolver) {
        return resolver.apply(claims);
    }

    public String generateToken(UserDto user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(getSignInKey())
                .compact();
    }

    public Optional<Claims> extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(claims);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public Optional<Authentication> getAuthentication(String jwtToken, Claims claims) {
        try {
            Long id = Long.valueOf(extractClaim(claims, Claims::getSubject));
            User user = userService.getUserById(id);
            if (user == null) {
                return Optional.empty();
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    jwtToken,
                    user.getAuthorities()
            );
            return Optional.of(authentication);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
