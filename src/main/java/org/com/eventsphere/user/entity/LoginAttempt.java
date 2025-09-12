package org.com.eventsphere.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_login_attempt_email", columnList = "email")
})
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private boolean successful;
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
