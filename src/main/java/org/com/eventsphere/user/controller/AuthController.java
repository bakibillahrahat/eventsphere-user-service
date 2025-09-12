package org.com.eventsphere.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.*;
import org.com.eventsphere.user.entity.RefreshToken;
import org.com.eventsphere.user.exception.TokenRefreshException;
import org.com.eventsphere.user.service.JwtService;
import org.com.eventsphere.user.service.RefreshTokenService;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received request to register user: {}", request.getEmail());
        UserResponse createdUser = userService.registerUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for user: {}", request.getEmail());
        AuthenticationResponse authenticationResponse = userService.loginUser(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    /**
     * REWRITTEN: This endpoint is now much cleaner. It simply delegates the entire
     * token refresh process to the transactional method in RefreshTokenService.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received request to refresh token.");
        AuthenticationResponse response = refreshTokenService.generateNewAccessToken(request.getToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        log.info("Received email verification request for token: {}", token);
        userService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully! You can now log in to your account.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Received password reset request for email: {}", request.getEmail());
        String result = userService.initiatePasswordReset(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordUpdateRequest request) {
        log.info("Received password update request.");
        String result = userService.resetPassword(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received logout request");
        String result = userService.logoutUser(request.getToken());
        return ResponseEntity.ok(result);
    }
}
