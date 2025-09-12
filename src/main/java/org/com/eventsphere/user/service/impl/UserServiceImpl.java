package org.com.eventsphere.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.*;
import org.com.eventsphere.user.entity.RefreshToken;
import org.com.eventsphere.user.entity.User;
import org.com.eventsphere.user.entity.VerificationToken;
import org.com.eventsphere.user.exception.EmailAlreadyExistsException;
import org.com.eventsphere.user.exception.InvalidCredentialsException;
import org.com.eventsphere.user.exception.TokenRefreshException;
import org.com.eventsphere.user.exception.UserNotFoundException;
import org.com.eventsphere.user.repository.UserRepository;
import org.com.eventsphere.user.repository.VerificationTokenRepository;
import org.com.eventsphere.user.service.EmailService;
import org.com.eventsphere.user.service.JwtService;
import org.com.eventsphere.user.service.RefreshTokenService;
import org.com.eventsphere.user.service.UserService;
import org.com.eventsphere.user.utils.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final EmailService emailService;
    private final JwtService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    // Authentication and Forgot Password will be added later.

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
                .isEmailVerified(false) // Email is not verified upon registration
                .build(); // The role defaults to USER thanks to @Builder.Default in the User entity.

        // 8. Save the new user to the database.
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // We will add the email verification logic here later.
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenValue)
                .user(savedUser)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        verificationTokenRepository.save(verificationToken);
        log.info("Verification token generated for user: {}", savedUser.getEmail());

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), tokenValue);
        log.info("Verification email sent to: {}", savedUser.getEmail());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public String verifyEmail(String token) {

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Invalid verification token."));

        if(verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new TokenRefreshException(token, "Verification token has expired.");
        }

        // Properly fetch the user to avoid lazy initialization issues
        User user = userRepository.findById(verificationToken.getUser().getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        log.info("Email verified successfully for user: {}", user.getEmail());
        return "Email verified successfully.";
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

        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);
        log.info("Refresh token generated for user: {}", user.getEmail());
        userRepository.updateLastLogin(user.getUserId(), LocalDateTime.now());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public String logoutUser(String refreshToken) {
        log.info("Processing logout request with refresh token");
        refreshTokenService.deleteByToken(refreshToken);
        log.info("User successfully logged out - refresh token invalidated");
        return "Successfully logged out";
    }

    @Override
    @Transactional
    public String initiatePasswordReset(PasswordResetRequest request) {
        log.info("Initiating password reset for email: {}", request.getEmail());
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String tokenValue = UUID.randomUUID().toString();
            VerificationToken resetToken = VerificationToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .type(VerificationToken.TokenType.PASSWORD_RESET)
                    .expiryDate(Instant.now().plusSeconds(900))
                    .build();
            verificationTokenRepository.save(resetToken);
            log.info("Password reset token generated for user: {}", user.getEmail());
            emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
            log.info("Password reset email sent to: {}", user.getEmail());
        });

        return "";
    }

    @Override
    @Transactional
    public String resetPassword(PasswordUpdateRequest request) {
        log.info("Attempting to reset password with token: {}", request.getToken());
        VerificationToken verificationToken = verificationTokenRepository.findByTokenWithUser(request.getToken())
                .orElseThrow(() -> new TokenRefreshException(request.getToken(), "Invalid password reset token."));

        if(verificationToken.isExpired() || verificationToken.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            verificationTokenRepository.delete(verificationToken);
            throw new TokenRefreshException(request.getToken(), "Password reset token has expired or is invalid.");
        }
        // user user builder
        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        log.info("Password reset successfully for user: {}", user.getEmail());
        return "Password reset successfully.";
    }



    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return userMapper.toUserResponse(user);
    }

    // User Profile Management methods to be implemented later.

    @Override
    public UserResponse updateUserProfile(Long id, UserProfileUpdateRequest request) {
        log.info("Updating user profile for user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        // Update only the fields that are provided in the request
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for user with ID: {}", id);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changeUserPassword(ChangePasswordRequest request, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database."));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed for user {}: Incorrect current password.", user.getEmail());
            throw new InvalidCredentialsException("Incorrect current password provided.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        refreshTokenService.deleteByUser(user);
        userRepository.deleteById(userId);
        log.info("User with ID: {} has been deleted successfully.", userId);
    }

    // Admin-specific method to fetch all users
    // This method should ideally be protected to be accessible only by admin users.

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        if (!users.isEmpty()) {
            return userMapper.toUserResponseList(users);
        }
        log.info("No users found in the database.");
        return List.of();
    }

}