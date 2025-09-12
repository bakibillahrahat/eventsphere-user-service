package org.com.eventsphere.user.service;

import org.com.eventsphere.user.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
//  User Registration, Authentication, Email Verification, Forgot Password, Reset Password
    UserResponse registerUser(UserRegistrationRequest request);
    AuthenticationResponse loginUser(LoginRequest loginRequest);
    String logoutUser(String refreshToken);
    String verifyEmail(String email);
    String initiatePasswordReset(PasswordResetRequest request);
    String resetPassword(PasswordUpdateRequest request);

    // user profile management
    UserResponse getUserById(Long id);
    UserResponse updateUserProfile(Long id, UserProfileUpdateRequest request);
    void changeUserPassword(ChangePasswordRequest request, UserDetails currentUser);
    void deleteUser(Long id);

    List<UserResponse> getAllUsers();
}
