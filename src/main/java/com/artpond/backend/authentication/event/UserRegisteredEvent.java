package com.artpond.backend.authentication.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final String email;
    private final String username;
}
