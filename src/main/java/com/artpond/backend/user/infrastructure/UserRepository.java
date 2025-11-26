package com.artpond.backend.user.infrastructure;

import com.artpond.backend.user.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT COUNT(p) > 0 FROM Publication p WHERE p.author.userId = :userId")
    Boolean userHasPublications(@Param("userId") Long userId);

    @Query("SELECT f FROM User u JOIN u.following f WHERE u.userId = :userId")
    Page<User> findFollowing(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f FROM User u JOIN u.followers f WHERE u.userId = :userId")
    Page<User> findFollowers(@Param("userId") Long userId, Pageable pageable);
}
