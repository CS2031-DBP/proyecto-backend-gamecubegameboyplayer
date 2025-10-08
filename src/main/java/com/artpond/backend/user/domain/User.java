package com.artpond.backend.user.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
    // private String role;
}
