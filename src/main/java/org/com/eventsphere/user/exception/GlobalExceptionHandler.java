package org.com.eventsphere.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A central place to handle exceptions across the whole application.
 * This keeps the controllers clean and provides consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    // Handle EmailAlreadyExistsException globally
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles the custom UserNotFoundException and returns a 404 NOT_FOUND response.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    // Handle TokenRefreshException globally
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Object> handleTokenRefreshException(TokenRefreshException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * NEW: Handles authentication failures, such as when a user is not found.
     * This provides a clearer response to the client than a generic 500 error.
     * We return a 401 Unauthorized status to indicate a failed login attempt.
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Object> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                // Provide a generic message for security reasons.
                // We don't want to tell an attacker whether the username was right or wrong.
                "message", "Bad credentials"
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Handles validation errors from @Valid annotation and returns a 400 BAD_REQUEST response.
     * This method extracts all validation error messages and formats them into a clean response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Validation Failed",
                "messages", errors
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * A catch-all handler for any other unexpected exceptions.
     * It returns a generic 500 INTERNAL_SERVER_ERROR response to avoid leaking sensitive system details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        // It's a good practice to log the full stack trace for unexpected errors for debugging purposes.
        ex.printStackTrace();

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "An unexpected error occurred. Please try again later."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
