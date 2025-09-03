package org.com.eventsphere.user.service.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.UserRegistrationRequest;
import org.com.eventsphere.user.dto.UserResponse;
import org.com.eventsphere.user.entity.Role;
import org.com.eventsphere.user.entity.User;
import org.com.eventsphere.user.repository.UserRepository;
import org.com.eventsphere.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email {} is already taken", request.getEmail());
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // ✅ Fixed: encode password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhone())
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

//        Add the email verification code

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueAndEmailVerifiedTrue()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setRole(user.getRole().name());
        userResponse.setIsActive(user.getIsActive());
        userResponse.setIsEmailVerified(user.getEmailVerified());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setLastLogin(user.getLastLogin());
        return userResponse;
    }

    // TODO methods - implement as needed
    @Override
    public UserResponse updateUser(Long id, UserRegistrationRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deactivateUser(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void sendEmailVerification(String email) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean verifyEmail(String email, String token) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void initiatePasswordReset(String email) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean resetPassword(String email, String token, String newPassword) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UserResponse promoteToOrganizer(Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}