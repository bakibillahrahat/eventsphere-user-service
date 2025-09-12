package org.com.eventsphere.user.service;

import lombok.RequiredArgsConstructor;
import org.com.eventsphere.user.dto.AuthenticationResponse;
import org.com.eventsphere.user.entity.RefreshToken;
import org.com.eventsphere.user.entity.User;
import org.com.eventsphere.user.exception.TokenRefreshException;
import org.com.eventsphere.user.repository.RefreshTokenRepository;
import org.com.eventsphere.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000; // 7 days

    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user) {
        // Delete any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request.");
        }
        return token;
    }

    @Transactional
    public AuthenticationResponse generateNewAccessToken(String requestRefreshToken) {
        User user = findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in the database!"));

        String newAccessToken = jwtService.generateToken(user);

        // Using the independent UserMapper to build the response.
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(requestRefreshToken)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
