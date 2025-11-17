package com.artpond.backend.publication.exception;

import com.artpond.backend.definitions.exception.BadRequestException;

public class PublicationCreationException extends BadRequestException {
    public PublicationCreationException() {
        super("The publication was not found.");
    }

    public PublicationCreationException(String message) {
        super(message);
    }
}