package com.stdiscm.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.ProfileCreationRequest;
import com.stdiscm.common.dto.UserProfileDto;
import com.stdiscm.common.exception.ResourceNotFoundException;
import com.stdiscm.profile.model.UserProfile;
import com.stdiscm.profile.model.UserType;
import com.stdiscm.profile.service.ProfileService;
import javax.validation.Valid; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Header names injected by the gateway
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String ADMIN_ROLE = "ADMIN";

    /**
     * Retrieves the profile for the specified user ID.
     * Only the user themselves or an admin can access the profile.
     *
     * @param userId The ID of the user whose profile to retrieve.
     * @param requestUserId The ID of the user making the request (from header).
     * @param requestUserRoles The roles of the user making the request (from header).
     * @return ResponseEntity containing the UserProfileDto or an error.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfileByUserId( // Return DTO
            @PathVariable Long userId,
            @RequestHeader(HEADER_USER_ID) Long requestUserId,
            @RequestHeader(HEADER_USER_ROLES) String requestUserRoles) {

        // Authorization Check: User must be fetching their own profile OR be an admin
        if (!userId.equals(requestUserId) && !hasRole(requestUserRoles, ADMIN_ROLE)) {
             throw new AccessDeniedException("User does not have permission to access this profile.");
        }

        UserProfile profile = profileService.getProfileByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        // Map UserProfile entity to UserProfileDto
        UserProfileDto profileDto = mapToDto(profile); 

        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profileDto));
    }

    /**
     * Creates a new profile using data from ProfileCreationRequest.
     * This endpoint is primarily used by the frontend service for initial profile creation.
     *
     * @param userId The ID of the user whose profile to create.
     * @param profileRequest The profile creation data.
     * @param requestUserId The ID of the user making the request (optional for internal calls).
     * @param requestUserRoles The roles of the user making the request (optional for internal calls).
     * @return ResponseEntity containing the saved UserProfileDto.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> saveProfile(
            @PathVariable Long userId,
            @Valid @RequestBody Object profileData,  // Accept any object type to handle both DTOs
            @RequestHeader(name = HEADER_USER_ID, required = false) Long requestUserId,
            @RequestHeader(name = HEADER_USER_ROLES, required = false) String requestUserRoles) {

        // Authorization Check: Only perform if headers are present (external call)
        if (requestUserId != null && requestUserRoles != null) {
            if (!userId.equals(requestUserId) && !hasRole(requestUserRoles, ADMIN_ROLE)) {
                throw new AccessDeniedException("User does not have permission to save this profile.");
            }
        } // If headers are null, it's an internal call, skip auth check.

        // Check if profile already exists
        Optional<UserProfile> existingProfileOpt = profileService.getProfileByUserId(userId);
        UserProfile profileToSave;
        String message;

        if (existingProfileOpt.isPresent()) {
            // Update existing profile
            profileToSave = existingProfileOpt.get();
            
            // Determine which type of object we received and handle accordingly
            if (profileData instanceof ProfileCreationRequest) {
                // Basic profile creation from auth service
                ProfileCreationRequest profileRequest = (ProfileCreationRequest) profileData;
                updateProfileFromCreationRequest(profileToSave, profileRequest);
            } else {
                // Full profile update from frontend
                try {
                    // Convert the Object to UserProfileDto - this handles the case when frontend sends all fields
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.findAndRegisterModules(); // For LocalDate support
                    UserProfileDto profileDto = mapper.convertValue(profileData, UserProfileDto.class);
                    updateProfileFromDto(profileToSave, profileDto);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid profile data format: " + e.getMessage());
                }
            }
            message = "Profile updated successfully";
        } else {
            // Create new profile
            profileToSave = new UserProfile();
            profileToSave.setUserId(userId);
            
            if (profileData instanceof ProfileCreationRequest) {
                // Basic profile creation
                ProfileCreationRequest profileRequest = (ProfileCreationRequest) profileData;
                profileToSave.setUserType(UserType.valueOf(profileRequest.getUserType()));
                profileToSave.setFirstName(profileRequest.getFirstName());
                profileToSave.setLastName(profileRequest.getLastName());
                profileToSave.setPrimaryEmail(profileRequest.getPrimaryEmail());
            } else {
                // Full profile creation (unlikely, but handle it)
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.findAndRegisterModules(); // For LocalDate support
                    UserProfileDto profileDto = mapper.convertValue(profileData, UserProfileDto.class);
                    profileToSave.setUserType(UserType.valueOf(profileDto.getUserType()));
                    updateProfileFromDto(profileToSave, profileDto);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid profile data format: " + e.getMessage());
                }
            }
            message = "Profile created successfully";
        }

        UserProfile savedProfile = profileService.saveProfile(userId, profileToSave);
        UserProfileDto savedProfileDto = mapToDto(savedProfile);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, message, savedProfileDto));
    }

    /**
     * Updates an existing profile with detailed information from UserProfileDto.
     * This endpoint is used for updating complete profile information after initial creation.
     *
     * @param userId The ID of the user whose profile to update.
     * @param profileDto The complete profile data.
     * @param requestUserId The ID of the user making the request.
     * @param requestUserRoles The roles of the user making the request.
     * @return ResponseEntity containing the saved UserProfileDto.
     */
    @PutMapping("/{userId}/details")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfileDetails(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDto profileDto,
            @RequestHeader(name = HEADER_USER_ID, required = false) Long requestUserId,
            @RequestHeader(name = HEADER_USER_ROLES, required = false) String requestUserRoles) {

        // Authorization Check: Only perform if headers are present (external call)
        if (requestUserId != null && requestUserRoles != null) {
             if (!userId.equals(requestUserId) && !hasRole(requestUserRoles, ADMIN_ROLE)) {
                 throw new AccessDeniedException("User does not have permission to save this profile.");
             }
        } // If headers are null, it's an internal call, skip auth check.

        UserProfile profileToSave;
        String message;
        
        // --- Check if profile exists, if not create a new one ---
        Optional<UserProfile> existingProfileOpt = profileService.getProfileByUserId(userId);
        
        if (existingProfileOpt.isPresent()) {
            // Update existing profile with data from DTO
            profileToSave = existingProfileOpt.get();
            updateProfileFromDto(profileToSave, profileDto);
            message = "Profile updated successfully";
        } else {
            // Create new profile from DTO
            profileToSave = new UserProfile();
            profileToSave.setUserId(userId);
            updateProfileFromDto(profileToSave, profileDto);
            message = "Profile created successfully";
        }

        UserProfile savedProfile = profileService.saveProfile(userId, profileToSave);

        // Map saved UserProfile entity to UserProfileDto
        UserProfileDto savedProfileDto = mapToDto(savedProfile);

        return ResponseEntity.status(HttpStatus.OK) // Always OK status for update or create
               .body(new ApiResponse<>(true, message, savedProfileDto));
    }

    /**
     * Helper method to check if the comma-separated roles string contains a specific role.
     */
    private boolean hasRole(String rolesHeader, String roleToCheck) {
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return false;
        }
        List<String> roles = Arrays.asList(rolesHeader.split("\\s*,\\s*"));
        return roles.contains(roleToCheck);
    }
    /**
     * Helper method to update a UserProfile entity with data from a ProfileCreationRequest.
     * This is used for basic profile initialization.
     *
     * @param profile The entity to update.
     * @param request The request containing the new data.
     */
    private void updateProfileFromCreationRequest(UserProfile profile, ProfileCreationRequest request) {
        if (request.getUserType() != null) {
            try {
                profile.setUserType(UserType.valueOf(request.getUserType()));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid UserType received in request: " + request.getUserType());
            }
        }
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getPrimaryEmail() != null) profile.setPrimaryEmail(request.getPrimaryEmail());
    }

    /**
     * Helper method to update an existing UserProfile entity with data from a UserProfileDto.
     * Only updates fields that are not null in the DTO to avoid overwriting existing data unintentionally.
     *
     * @param existingProfile The entity fetched from the database.
     * @param dto The DTO containing the new data.
     */
    private void updateProfileFromDto(UserProfile existingProfile, UserProfileDto dto) {
        // Update only non-null fields from the DTO
        if (dto.getUserType() != null) {
             try {
                 existingProfile.setUserType(UserType.valueOf(dto.getUserType()));
             } catch (IllegalArgumentException e) {
                 System.err.println("Warning: Invalid UserType received in DTO: " + dto.getUserType());
             }
        }
        if (dto.getFirstName() != null) existingProfile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existingProfile.setLastName(dto.getLastName());
        if (dto.getPreferredName() != null) existingProfile.setPreferredName(dto.getPreferredName());
        if (dto.getDateOfBirth() != null) existingProfile.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) existingProfile.setGender(dto.getGender());
        if (dto.getPrimaryEmail() != null) existingProfile.setPrimaryEmail(dto.getPrimaryEmail());
        if (dto.getSecondaryEmail() != null) existingProfile.setSecondaryEmail(dto.getSecondaryEmail());
        if (dto.getMobilePhone() != null) existingProfile.setMobilePhone(dto.getMobilePhone());
        if (dto.getAddressStreet() != null) existingProfile.setAddressStreet(dto.getAddressStreet());
        if (dto.getAddressCity() != null) existingProfile.setAddressCity(dto.getAddressCity());
        if (dto.getAddressState() != null) existingProfile.setAddressState(dto.getAddressState());
        if (dto.getAddressZipCode() != null) existingProfile.setAddressZipCode(dto.getAddressZipCode());
        if (dto.getAddressCountry() != null) existingProfile.setAddressCountry(dto.getAddressCountry());
        if (dto.getEmergencyContactName() != null) existingProfile.setEmergencyContactName(dto.getEmergencyContactName());
        if (dto.getEmergencyContactRelationship() != null) existingProfile.setEmergencyContactRelationship(dto.getEmergencyContactRelationship());
        if (dto.getEmergencyContactPhone() != null) existingProfile.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        if (dto.getStudentId() != null) existingProfile.setStudentId(dto.getStudentId());
        if (dto.getMajor() != null) existingProfile.setMajor(dto.getMajor());
        if (dto.getEmployeeId() != null) existingProfile.setEmployeeId(dto.getEmployeeId());
        if (dto.getDepartment() != null) existingProfile.setDepartment(dto.getDepartment());
        if (dto.getTitle() != null) existingProfile.setTitle(dto.getTitle());
        // Note: userId, profileId, createdAt, updatedAt are generally not updated from DTO
    }


    // Manual mapping from Entity to DTO (Consider MapStruct for complex objects)
    private UserProfileDto mapToDto(UserProfile profile) {
        if (profile == null) return null;
        // Manual mapping example (replace with actual implementation)
        UserProfileDto dto = new UserProfileDto();
        dto.setProfileId(profile.getProfileId());
        dto.setUserId(profile.getUserId());
        dto.setUserType(profile.getUserType() != null ? profile.getUserType().name() : null);
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPreferredName(profile.getPreferredName());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setPrimaryEmail(profile.getPrimaryEmail());
        dto.setSecondaryEmail(profile.getSecondaryEmail());
        dto.setMobilePhone(profile.getMobilePhone());
        dto.setAddressStreet(profile.getAddressStreet());
        dto.setAddressCity(profile.getAddressCity());
        dto.setAddressState(profile.getAddressState());
        dto.setAddressZipCode(profile.getAddressZipCode());
        dto.setAddressCountry(profile.getAddressCountry());
        dto.setEmergencyContactName(profile.getEmergencyContactName());
        dto.setEmergencyContactRelationship(profile.getEmergencyContactRelationship());
        dto.setEmergencyContactPhone(profile.getEmergencyContactPhone());
        dto.setStudentId(profile.getStudentId());
        dto.setMajor(profile.getMajor());
        dto.setEmployeeId(profile.getEmployeeId());
        dto.setDepartment(profile.getDepartment());
        dto.setTitle(profile.getTitle());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
