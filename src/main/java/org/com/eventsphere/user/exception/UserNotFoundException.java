package org.com.eventsphere.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for when a user is requested but cannot be found in the database.
 * The @ResponseStatus annotation tells Spring to return a 404 Not Found HTTP status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // This will produce a 404 Not Found status code
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
