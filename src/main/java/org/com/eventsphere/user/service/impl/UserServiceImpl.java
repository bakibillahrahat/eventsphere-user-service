package org.com.eventsphere.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.*;
import org.com.eventsphere.user.entity.*;
import org.com.eventsphere.user.exception.EmailAlreadyExistsException;
import org.com.eventsphere.user.exception.InvalidCredentialsException;
import org.com.eventsphere.user.exception.TokenRefreshException;
import org.com.eventsphere.user.exception.UserNotFoundException;
import org.com.eventsphere.user.repository.LoginAttemptRepository;
import org.com.eventsphere.user.repository.UserRepository;
import org.com.eventsphere.user.repository.VerificationTokenRepository;
import org.com.eventsphere.user.service.EmailService;
import org.com.eventsphere.user.service.JwtService;
import org.com.eventsphere.user.service.RefreshTokenService;
import org.com.eventsphere.user.service.UserService;
import org.com.eventsphere.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final LoginAttemptRepository loginAttemptRepository;

    // Authentication & User Lifecycle methods
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

    // Password Management methods

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

    // User Profile Management methods

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
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

    @Override
    public void assignRoleToUser(Long userId, String roleName) {
        log.info("Attempting to assign role '{}' to user with ID: {}", roleName, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        Role newRole;
        try {
            newRole = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            log.warn("Role assignment failed: Invalid role name '{}'.", roleName);
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }
        user.setRole(newRole);
        userRepository.save(user);
        log.info("Role '{}' assigned to user with ID: {} successfully.", roleName, userId);
    }

    @Override
    public void removeRoleFromUser(Long userId, String roleName) {
        log.info("Removing role '{}' from user with ID: {}", roleName, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        Role roleToRemove;
        try {
            roleToRemove = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            log.warn("Role removal failed: Invalid role name '{}'.", roleName);
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }
        if (user.getRole() == roleToRemove) {
            user.setRole(Role.USER); // Default to USER role if the current role is being removed
            userRepository.save(user);
            log.info("Role '{}' removed from user with ID: {}. Defaulted to USER role.", roleName, userId);
        } else {
            log.info("User with ID: {} does not have role '{}'. No changes made.", userId, roleName);
        }
        log.info("User with ID: {} has been removed successfully.", userId);
    }

    @Override
    public List<UserResponse> getUsersByRole(String roleName) {
        log.info("Fetching users with role: {}", roleName);
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            log.warn("Fetching users by role failed: Invalid role name '{}'.", roleName);
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }
        log.info("Fetching users by role: {}", role);

        return userRepository.findByRole(role).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        user.setRole(Role.USER);
        userRepository.save(user);
        log.info("User with ID: {} has been deactivated.", id);
    }

    @Override
    public void reactivateUser(Long id) {
        log.info("Reactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        user.setActive(true);
        userRepository.save(user);
        log.info("User with ID: {} has been reactivated.", id);
    }

      // Advanced Search & Reporting methods

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        log.info("Fetching users with query: {}", query);

        return userRepository.searchByFirstNameLastNameOrEmail(query).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getInactiveUsers() {
        log.info("Fetching inactive users");
        // Assuming inactive users are those who are not active
        List<User> inactiveUsers = userRepository.findByIsActive(false);
        if (!inactiveUsers.isEmpty()) {
            return userMapper.toUserResponseList(inactiveUsers);
        }
        log.info("No inactive users found in the database.");
        return List.of();
    }

    @Override
    public List<UserResponse> getUsersRegisteredBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        log.info("Fetching users between {} and {}", startDate, endDate);
        return userRepository.findByCreatedAtBetween(startDateTime, endDateTime).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersLastActiveBefore(LocalDateTime dateTime) {
        log.info("Fetching users last active before {}", dateTime);
        return userRepository.findByLastLoginAtBefore(dateTime).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void purgeInactiveUsers(int months) {
        log.info("Purging inactive users older than {} months", months);
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(months);
        log.warn("Purging users older than {} months", cutoff);
        userRepository.deleteUnverifiedUsersBefore(cutoff);
        log.info("Purge of inactive users completed.");
    }

    @Override
    public void purgeUnverifiedUsers(int hours) {
        log.info("Purging unverified users older than {} hours", hours);
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        log.warn("Purging users older than {} hours", cutoff);
        userRepository.deleteUnverifiedUsersBefore(cutoff);
        log.info("Purge of unverified users completed.");
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("Attempting to resend verification email to {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            log.info("User with email {} is already verified. No email sent.", email);
            return;
        }

        // Generate a new token and send the email
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(Instant.now().plusSeconds(86400)) // 24 hours
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), tokenValue);
        log.info("Successfully resent verification email to {}", email);
    }

    @Override
    public void updateUserEmail(Long userId, String newEmail) {
        log.info("Updating email for user ID: {} to new email: {}", userId, newEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        if (userRepository.findByEmail(newEmail).isPresent()) {
            log.warn("Email update failed: Email {} is already taken.", newEmail);
            throw new EmailAlreadyExistsException("Error: Email '" + newEmail + "' is already in use!");
        } else {
            user.setEmail(newEmail);
            user.setEmailVerified(false); // Require re-verification for new email
            userRepository.save(user);
            log.info("User email updated successfully to {}. Verification required.", newEmail);

            // Generate and send a new verification email
            String tokenValue = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                    .expiryDate(Instant.now().plusSeconds(86400)) // 24 hours
                    .build();
            verificationTokenRepository.save(verificationToken);
            emailService.sendVerificationEmail(newEmail, tokenValue);
            log.info("Verification email sent to new address: {}", newEmail);
        }
    }

    @Override
    public void updateUserLastActive(String email) {
        log.info("Updating last active email for user ID: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        userRepository.updateLastLogin(user.getUserId(), LocalDateTime.now());
        log.info("User last active time updated successfully for email: {}", email);
    }

    // Login Attempt Tracking (Security) methods

    @Override
    public void recordLoginAttempt(String email, boolean successful) {
        log.info("Recording login attempt for email: {}. Successful: {}", email, successful);
        // Create LoginAttempt using the builder pattern
        LoginAttempt loginAttempt = LoginAttempt.builder()
                .email(email)
                .successful(successful)
                .build();
        loginAttemptRepository.save(loginAttempt);
        log.info("Login attempt recorded for email: {}", email);
    }

    @Override
    public List<LoginAttemptResponse> getLoginAttempts(String email) {
        log.info("Fetching login attempts for email: {}", email);
        List<LoginAttempt> loginAttempts = loginAttemptRepository.findByEmailOrderByTimestampDesc(email);

        return loginAttempts.stream()
                .map(attempt -> new LoginAttemptResponse(
                        attempt.getId(),
                        attempt.getEmail(),
                        attempt.isSuccessful(),
                        attempt.getTimestamp()
                ))
                .collect(Collectors.toList());
    }
}
