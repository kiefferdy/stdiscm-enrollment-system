package com.stdiscm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long profileId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "User type cannot be blank")
    private String userType;

    // --- Personal Info ---
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    private String preferredName;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    private String gender;

    // --- Contact Info ---
    @NotBlank(message = "Primary email cannot be blank")
    @Email(message = "Invalid email format")
    private String primaryEmail;

    @Email(message = "Invalid email format")
    private String secondaryEmail;

    private String mobilePhone;

    // --- Address ---
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressZipCode;
    private String addressCountry;

    // --- Emergency Contact ---
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    // --- Role-Specific Info ---
    private String studentId;
    private String major;
    private String employeeId;
    private String department;
    private String title;

    // --- Timestamps ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
