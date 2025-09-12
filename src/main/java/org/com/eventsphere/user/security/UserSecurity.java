package org.com.eventsphere.user.security;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * UserSecurity
 * This component provides security-related utility methods for authorization checks.
 * It's used in @PreAuthorize annotations to verify user permissions.
 */
@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private static final Logger log = LoggerFactory.getLogger(UserSecurity.class);

    private final UserRepository userRepository;

    /**
     * Gets the user ID from the authenticated principal.
     *
     * @param principal The authenticated user details
     * @return The user ID of the authenticated user
     */
    public Long getUserId(UserDetails principal) {
        log.debug("Getting user ID for principal: {}", principal.getUsername());

        return userRepository.findByEmail(principal.getUsername())
                .map(user -> user.getUserId())
                .orElse(null);
    }

    /**
     * Checks if the authenticated user can access the specified user ID.
     * Users can access their own data, and admins can access any user's data.
     *
     * @param userId The target user ID
     * @param principal The authenticated user details
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessUser(Long userId, UserDetails principal) {
        Long authenticatedUserId = getUserId(principal);

        if (authenticatedUserId == null) {
            log.warn("Could not determine user ID for principal: {}", principal.getUsername());
            return false;
        }

        boolean canAccess = authenticatedUserId.equals(userId);
        log.debug("User {} can access user {}: {}", authenticatedUserId, userId, canAccess);

        return canAccess;
    }
}
