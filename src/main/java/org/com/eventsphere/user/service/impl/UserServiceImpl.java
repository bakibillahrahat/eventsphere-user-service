package org.com.eventsphere.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.AuthenticationResponse;
import org.com.eventsphere.user.dto.LoginRequest;
import org.com.eventsphere.user.dto.UserRegistrationRequest;
import org.com.eventsphere.user.dto.UserResponse;
import org.com.eventsphere.user.entity.User;
import org.com.eventsphere.user.exception.EmailAlreadyExistsException;
import org.com.eventsphere.user.exception.UserNotFoundException;
import org.com.eventsphere.user.repository.UserRepository;
import org.com.eventsphere.user.service.JwtService;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserServiceImpl
 * The concrete implementation of the UserService interface. This is where the core business logic resides.
 */

@Service // 1. Marks this class as a Spring service bean, making it available for dependency injection.
@RequiredArgsConstructor // 2. Lombok annotation to create a constructor with all final fields.
public class UserServiceImpl implements UserService {
    // A logger for logging messages. It's a best practice for debugging and monitoring.
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // 3. Injecting dependencies via constructor. These are the tools our service needs.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @Override
    @Transactional // 4. Ensures the entire method runs within a single database transaction.
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // 5. Business Rule: Check if a user with this email already exists.
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already taken.", request.getEmail());
            throw new EmailAlreadyExistsException("Error: Email '" + request.getEmail() + "' is already in use!");
        }

        // 6. Create a new User entity from the request DTO.
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                // 7. Security: NEVER store passwords in plain text. Always hash them.
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhone())
                .isActive(true) // User is active by default
                .isEmailVerified(true) // Email is not verified upon registration
                .build(); // The role defaults to USER thanks to @Builder.Default in the User entity.

        // 8. Save the new user to the database.
        User savedUser = userRepository.save(user);
        // We will add the email verification logic here later.

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthenticationResponse loginUser(LoginRequest loginRequest) {
        log.info("Attempting to authenticate user: {}", loginRequest.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found after successful authentication"));
        // We will add JWT generation logic here later.
        String jwtToken = jwtService.generateToken(user);
        log.info("JWT generated for user: {}", user.getEmail());
        userRepository.updateLastLogin(user.getUserId(), LocalDateTime.now());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    /**
     * A private helper method to map a User entity to a UserResponse DTO.
     * This keeps our main logic clean and handles the conversion in one place.
     */

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .isEmailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}