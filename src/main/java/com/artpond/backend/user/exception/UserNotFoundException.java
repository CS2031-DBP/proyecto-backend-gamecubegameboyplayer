package com.artpond.backend.user.exception;
import com.artpond.backend.definitions.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User ID was not found in data.");
    }
}
