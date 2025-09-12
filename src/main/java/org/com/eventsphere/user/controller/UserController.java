package org.com.eventsphere.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.ChangePasswordRequest;
import org.com.eventsphere.user.dto.UserProfileUpdateRequest;
import org.com.eventsphere.user.dto.UserResponse;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @userSecurity.getUserId(principal)")
    public ResponseEntity<UserResponse> updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateRequest request){
        log.info("Received request to update user profile: {}", request);
        UserResponse updatedUser = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changeUserPassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails principal) {
        log.info("Received request to change password for user: {}", principal.getUsername());
        userService.changeUserPassword(request, principal);
        return ResponseEntity.ok("Password changed successfully.");
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @userSecurity.getUserId(principal)")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user profile: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User with ID " + id + " has been deleted successfully.");
    }

}
