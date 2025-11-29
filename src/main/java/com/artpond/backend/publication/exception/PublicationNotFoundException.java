package com.artpond.backend.publication.exception;

import com.artpond.backend.definitions.exception.NotFoundException;

public class PublicationNotFoundException extends NotFoundException {
    public PublicationNotFoundException() {
        super("The publication was not found.");
    }
}
