package org.com.eventsphere.user.repository;

import org.com.eventsphere.user.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String verificationToken);

    @Query("SELECT vt FROM VerificationToken vt JOIN FETCH vt.user WHERE vt.token = :token")
    Optional<VerificationToken> findByTokenWithUser(@Param("token") String token);
}
