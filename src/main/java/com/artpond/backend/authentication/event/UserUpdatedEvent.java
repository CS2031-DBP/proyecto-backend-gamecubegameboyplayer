package com.artpond.backend.authentication.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdatedEvent {
    private final String email;
    private final String username;
}
