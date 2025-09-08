package org.com.eventsphere.user.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for when a registration attempt is made with an email that already exists.
 * The @ResponseStatus annotation tells Spring to automatically return a 409 Conflict HTTP status
 * whenever this exception is thrown and not caught elsewhere.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
