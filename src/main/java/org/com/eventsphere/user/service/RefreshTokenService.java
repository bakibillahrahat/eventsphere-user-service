package org.com.eventsphere.user.service;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.entity.RefreshToken;
import org.com.eventsphere.user.entity.User;
import org.com.eventsphere.user.repository.RefreshTokenRepository;
import org.com.eventsphere.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000; // 7 days

    /**
     * Creates a new refresh token for a user and saves it to the database.
     * It ensures that if a user already has a token, it gets replaced.
     *
     * @param user The user for whom to create the token.
     * @return The newly created RefreshToken entity.
     */

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken = RefreshToken.builder().user(user).token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
