package org.com.eventsphere.user.controller;

import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{id}")
    public  ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by id: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }
}
