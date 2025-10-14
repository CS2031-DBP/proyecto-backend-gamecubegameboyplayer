package com.artpond.backend.definitions.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        this("Requested item not found.");
    }

    public NotFoundException(final String message) {
        super(message);
    }
}
