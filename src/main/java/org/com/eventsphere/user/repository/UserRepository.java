package org.com.eventsphere.user.repository;

import org.com.eventsphere.user.entity.Role;
import org.com.eventsphere.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);


    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE lower(u.firstName) LIKE lower(concat('%', :query, '%')) OR lower(u.lastName) LIKE lower(concat('%', :query, '%')) OR lower(u.email) LIKE lower(concat('%', :query, '%'))")
    List<User> searchByFirstNameLastNameOrEmail(@Param("query") String query);
    List<User> findByIsActive(boolean isActive);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM User u WHERE u.isEmailVerified = false AND u.createdAt < :cutoff")
    void deleteUnverifiedUsersBefore(@Param("cutoff") LocalDateTime cutoff);
}
