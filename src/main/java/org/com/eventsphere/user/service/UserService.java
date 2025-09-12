package org.com.eventsphere.user.service;

import org.com.eventsphere.user.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserService {

    // --- Core Authentication & User Lifecycle ---
    UserResponse registerUser(UserRegistrationRequest request);
    String verifyEmail(String email);
    AuthenticationResponse loginUser(LoginRequest loginRequest);
    String logoutUser(String refreshToken);

    // --- Password Management ---
    String initiatePasswordReset(PasswordResetRequest request);
    String resetPassword(PasswordUpdateRequest request);
    void changeUserPassword(ChangePasswordRequest request, UserDetails currentUser);

    // --- User Profile Management ---
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse updateUserProfile(Long id, UserProfileUpdateRequest request);
    void deleteUser(Long id);

    // admin functionalities
    List<UserResponse> getAllUsers();
    void assignRoleToUser(Long userId, String roleName);
    void removeRoleFromUser(Long userId, String roleName);
    List<UserResponse> getUsersByRole(String roleName);
    void deactivateUser(Long id);
    void reactivateUser(Long id);

    // --- Advanced Search & Reporting ---
    List<UserResponse> searchUsers(String query);
    List<UserResponse> getInactiveUsers();
    List<UserResponse> getUsersRegisteredBetween(LocalDate startDate, LocalDate endDate);
    List<UserResponse> getUsersLastActiveBefore(LocalDateTime dateTime);

    // --- Maintenance & Other Utilities ---
    void purgeInactiveUsers(int months);
    void purgeUnverifiedUsers(int hours);
    void resendVerificationEmail(String email);
    void updateUserEmail(Long userId, String newEmail);
    void updateUserLastActive(String email);

    // --- Login Attempt Tracking (Security) ---
    void recordLoginAttempt(String email, boolean successful);
    List<LoginAttemptResponse> getLoginAttempts(String email);
}
