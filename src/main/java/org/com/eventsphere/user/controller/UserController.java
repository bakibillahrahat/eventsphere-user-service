package org.com.eventsphere.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.AuthenticationResponse;
import org.com.eventsphere.user.dto.LoginRequest;
import org.com.eventsphere.user.dto.UserRegistrationRequest;
import org.com.eventsphere.user.dto.UserResponse;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController
 * This class acts as the entry point for all user-related API requests.
 * It is responsible for handling HTTP requests, delegating business logic to the UserService,
 * and returning an appropriate HTTP response.
 */

@RestController // 1. Marks this class as a REST controller, which combines @Controller and @ResponseBody.
@RequestMapping("/api/v1/users") // 2. Sets a base path for all methods in this controller.
@RequiredArgsConstructor // 3. Injects dependencies (like UserService) via the constructor.
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * API endpoint for registering a new user.
     * URL: POST http://localhost:8081/api/v1/users/register
     *
     * @param request The request body containing user details, mapped to UserRegistrationRequest DTO.
     * @return An HTTP response with the created user's data and a 201 CREATED status.
     */

    @PostMapping("/register") // 4. Maps HTTP POST requests to this method.
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        // 5. The @Valid annotation triggers the validation rules in the UserRegistrationRequest DTO.
        // 6. The @RequestBody annotation tells Spring to deserialize the incoming JSON body into our DTO.
        log.info("Received registration request for email: {}", request.getEmail());

        // 7. Delegate the actual business logic to the service layer.
        UserResponse userResponse = userService.registerUser(request);

        // 8. Return a successful HTTP response (201 CREATED) with the new user's data in the body.
        return ResponseEntity.status(201).body(userResponse);
    }

    /**
     * API endpoint for user login.
     * URL: POST http://localhost:8081/api/v1/users/login
     *
     * @param request The request body containing the user's email and password.
     * @return An HTTP response with the JWT and user details.
     */

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());

        // Delegate the actual business logic to the service layer.
        AuthenticationResponse authResponse = userService.loginUser(loginRequest);

        // Return a successful HTTP response (200 OK) with the authentication details in the body.
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/{id}")
    public  ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by id: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }
}
