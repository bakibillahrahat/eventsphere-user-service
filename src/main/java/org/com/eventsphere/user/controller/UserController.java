package org.com.eventsphere.user.controller;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.UserResponse;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * GET /api/v1/users
     * Fetches a list of all users.
     *
     * @return A ResponseEntity containing a list of UserResponse DTOs and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Getting all users.");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/v1/users/{id}
     * Fetches a user by their unique ID.
     *
     * @param id The unique identifier of the user to be fetched.
     * @return A ResponseEntity containing the UserResponse DTO and HTTP status 200 (OK).
     *         If the user is not found, it returns HTTP status 404 (Not Found).
     */

    @GetMapping("/{id}")
    public  ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by id: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }
}
