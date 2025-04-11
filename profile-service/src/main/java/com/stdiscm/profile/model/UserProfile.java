package com.stdiscm.profile.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_user_id", columnList = "userId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @NotNull(message = "User ID cannot be null")
    @Column(nullable = false, unique = true)
    private Long userId;

    @NotNull(message = "User type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType userType;

    // --- Personal Info ---
    @NotBlank(message = "First name cannot be blank")
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String preferredName;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String gender;

    // --- Contact Info ---
    @NotBlank(message = "Primary email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(nullable = false, length = 255)
    private String primaryEmail;

    @Email(message = "Invalid email format")
    @Column(length = 255)
    private String secondaryEmail;

    @Column(length = 30)
    private String mobilePhone;

    // --- Address (Simplified for now, could be embedded or separate entity) ---
    @Column(length = 255)
    private String addressStreet;
    @Column(length = 100)
    private String addressCity;
    @Column(length = 100)
    private String addressState;
    @Column(length = 20)
    private String addressZipCode;
    @Column(length = 100)
    private String addressCountry;

    // --- Emergency Contact (Simplified) ---
    @Column(length = 100)
    private String emergencyContactName;
    @Column(length = 50)
    private String emergencyContactRelationship;
    @Column(length = 30)
    private String emergencyContactPhone;

    // --- Role-Specific Info (Add fields as needed) ---
    // Example for Student
    @Column(length = 50)
    private String studentId; // Only relevant if userType is STUDENT
    @Column(length = 100)
    private String major;     // Only relevant if userType is STUDENT

    // Example for Faculty
    @Column(length = 50)
    private String employeeId; // Only relevant if userType is FACULTY
    @Column(length = 100)
    private String department; // Only relevant if userType is FACULTY
    @Column(length = 100)
    private String title;      // Only relevant if userType is FACULTY

    // --- Timestamps ---
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
}
