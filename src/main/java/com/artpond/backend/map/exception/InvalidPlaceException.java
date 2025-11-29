package com.artpond.backend.map.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidPlaceException extends RuntimeException {
    public InvalidPlaceException() {
        super("This place was invalid and not found in the OSM database.");
    }

    public InvalidPlaceException(String message) {
        super(message);
    }
}