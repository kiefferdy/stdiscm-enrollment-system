package com.stdiscm.profile.controller;

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
     * Creates or updates the profile for the specified user ID.
     * Only the user themselves or an admin can save the profile.
     *
     * @param userId The ID of the user whose profile to save.
     * @param profileDetails The profile data from the request body.
     * @param requestUserId The ID of the user making the request (from header).
     * @param requestUserRoles The roles of the user making the request (from header).
     * @return ResponseEntity containing the saved UserProfileDto or an error.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> saveProfile( // Return DTO
            @PathVariable Long userId,
            @Valid @RequestBody ProfileCreationRequest profileRequest,
            // Headers are optional to allow internal calls (e.g., from auth-service)
            @RequestHeader(name = HEADER_USER_ID, required = false) Long requestUserId,
            @RequestHeader(name = HEADER_USER_ROLES, required = false) String requestUserRoles) {

        // Authorization Check: Only perform if headers are present (external call)
        if (requestUserId != null && requestUserRoles != null) {
             if (!userId.equals(requestUserId) && !hasRole(requestUserRoles, ADMIN_ROLE)) {
                 throw new AccessDeniedException("User does not have permission to save this profile.");
             }
        } // If headers are null, it's an internal call, skip auth check.

        // Ensure the userId in the path matches the one in the body 
        if (!userId.equals(profileRequest.getUserId())) {
             return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "User ID in path does not match User ID in request body.", null));
        }

        // --- Map DTO to Entity ---
        UserProfile profileToSave = new UserProfile();
        profileToSave.setUserId(profileRequest.getUserId());
        profileToSave.setUserType(UserType.valueOf(profileRequest.getUserType()));
        profileToSave.setFirstName(profileRequest.getFirstName());
        profileToSave.setLastName(profileRequest.getLastName());
        profileToSave.setPrimaryEmail(profileRequest.getPrimaryEmail());
        // Note: Other fields remain null/default for initial creation via this DTO.
        // The service layer merges with existing data if the profile already exists.

        UserProfile savedProfile = profileService.saveProfile(userId, profileToSave);
        // Determine if it was a create or update for the response message (optional)
        boolean created = savedProfile.getCreatedAt().equals(savedProfile.getUpdatedAt());
        String message = created ? "Profile created successfully" : "Profile updated successfully";

        // Map saved UserProfile entity to UserProfileDto
        UserProfileDto savedProfileDto = mapToDto(savedProfile);

        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
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
