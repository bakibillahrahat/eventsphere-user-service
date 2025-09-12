package org.com.eventsphere.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttemptResponse {
    private Long id;
    private String email;
    private boolean successful;
    private LocalDateTime timestamp;
}
