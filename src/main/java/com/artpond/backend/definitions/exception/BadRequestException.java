package com.artpond.backend.definitions.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException() {
        this("Requested item not found.");
    }

    public BadRequestException(final String message) {
        super(message);
    }
}
