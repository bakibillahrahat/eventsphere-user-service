package org.com.eventsphere.user.service;

import org.com.eventsphere.user.dto.AuthenticationResponse;
import org.com.eventsphere.user.dto.LoginRequest;
import org.com.eventsphere.user.dto.UserRegistrationRequest;
import org.com.eventsphere.user.dto.UserResponse;

import java.util.List;

public interface UserService {
//  User Registration & Management
    UserResponse registerUser(UserRegistrationRequest request);
    /**
     * Authenticates a user and returns a JWT upon successful login.
     *
     * @param request A DTO containing the user's email and password.
     * @return A DTO containing the JWT and user details.
     */
    AuthenticationResponse loginUser(LoginRequest loginRequest);
    UserResponse getUserById(Long id);
    String verifyEmail(String email);


    List<UserResponse> getAllUsers();
}
