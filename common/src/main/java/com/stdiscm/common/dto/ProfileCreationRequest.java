package com.stdiscm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Using javax validation for compatibility with Spring Boot 2.7
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for sending basic profile information from auth-service
 * to profile-service upon user registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreationRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "User type cannot be blank")
    private String userType; // e.g., "STUDENT", "FACULTY"

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Primary email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String primaryEmail;

}
