package com.stdiscm.profile.controller;

import com.stdiscm.common.dto.ApiResponse; 
import com.stdiscm.common.dto.ProfileCreationRequest; 
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
     * @return ResponseEntity containing the UserProfile or an error.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfile>> getProfileByUserId(
            @PathVariable Long userId,
            @RequestHeader(HEADER_USER_ID) Long requestUserId,
            @RequestHeader(HEADER_USER_ROLES) String requestUserRoles) {

        // Authorization Check: User must be fetching their own profile OR be an admin
        if (!userId.equals(requestUserId) && !hasRole(requestUserRoles, ADMIN_ROLE)) {
             throw new AccessDeniedException("User does not have permission to access this profile.");
        }

        UserProfile profile = profileService.getProfileByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profile));
    }

    /**
     * Creates or updates the profile for the specified user ID.
     * Only the user themselves or an admin can save the profile.
     *
     * @param userId The ID of the user whose profile to save.
     * @param profileDetails The profile data from the request body.
     * @param requestUserId The ID of the user making the request (from header).
     * @param requestUserRoles The roles of the user making the request (from header).
     * @return ResponseEntity containing the saved UserProfile or an error.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfile>> saveProfile(
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

        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
               .body(new ApiResponse<>(true, message, savedProfile));
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

}
