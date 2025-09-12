package org.com.eventsphere.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttemptResponse {
    private String email;
    private boolean successful;
    private String timestamp;
}
