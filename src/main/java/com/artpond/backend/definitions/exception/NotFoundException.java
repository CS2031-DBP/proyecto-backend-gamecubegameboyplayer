package com.artpond.backend.definitions.exception;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ResponseStatusException {
    public NotFoundException(final String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
