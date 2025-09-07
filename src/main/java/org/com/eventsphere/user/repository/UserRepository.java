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
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
//    Optional<User> findByEmailAndIsActive(String email, boolean isActive);
//    List<User> findByRole(Role role);
//    List<User> findByIsActiveTrueAndEmailVerifiedTrue();
    boolean existsByEmail(String email);
//
//    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate")
//    List<User> findUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);
//
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
}
