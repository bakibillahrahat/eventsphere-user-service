package org.com.eventsphere.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserProfileUpdateRequest {
    @Size(max = 50, message = "First name cannot be longer than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot be longer than 50 characters")
    private String lastName;

    @Size(max = 20, message = "Phone number cannot be longer than 20 characters")
    private String phoneNumber;
}
