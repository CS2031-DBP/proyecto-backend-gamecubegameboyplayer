package com.artpond.backend.authentication.infrastructure;

import com.artpond.backend.authentication.domain.RefreshToken;
import com.artpond.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserOrderByExpiryDateAsc(User user);
    
    @Modifying
    int deleteByUser(User user);
}
