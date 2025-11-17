package com.artpond.backend.jwt.application;

import com.artpond.backend.jwt.domain.JwtService;
import com.artpond.backend.user.domain.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userDetailsService;

    @Override
    protected void doFilterInternal (@NonNull HttpServletRequest request, @NonNull HttpServletResponse response , @NonNull FilterChain filterChain )
            throws ServletException, IOException {
        final String authHeader = request.getHeader ("Authorization" );
        if (StringUtils .hasText(authHeader) && StringUtils .startsWithIgnoreCase (authHeader, "Bearer " )) {
            String token = authHeader .substring (7);
            if (jwtService .isTokenValid (token)) {
                String username = jwtService .extractUsername (token);
                if (StringUtils .hasText(username) && SecurityContextHolder .getContext ().getAuthentication () == null)
                {
                    UserDetails userDetails = userDetailsService .loadUserByUsername (username);
                    SecurityContext context = SecurityContextHolder .createEmptyContext ();
                    UsernamePasswordAuthenticationToken authToken = new
                            UsernamePasswordAuthenticationToken (userDetails, null, userDetails .getAuthorities ());
                    authToken .setDetails (new WebAuthenticationDetailsSource().buildDetails (request));
                    context.setAuthentication (authToken);
                    SecurityContextHolder .setContext (context);
                }
            }
        }
        // follow the chain
        filterChain .doFilter (request, response);
    }
}
