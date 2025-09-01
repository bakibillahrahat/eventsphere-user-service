package org.com.eventsphere.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid format")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Full-Name is required")
    private String fullName;
    @NotBlank
    private String role;
}
