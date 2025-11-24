package com.artpond.backend.authentication.domain;

import com.artpond.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity(name = "refreshtoken")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}