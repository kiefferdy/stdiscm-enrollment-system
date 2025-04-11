package com.stdiscm.frontend.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.ProfileCreationRequest;
import com.stdiscm.common.dto.UserProfileDto; // Use the common DTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "profile-service", path = "/api/profiles")
public interface ProfileServiceClient {

    /**
     * Retrieves the profile for the specified user ID.
     * Requires Authorization, X-User-Id, and X-User-Roles headers.
     *
     * @param userId The ID of the user whose profile to retrieve (in path).
     * @param bearerToken The full Authorization header value.
     * @param requestUserId The ID of the user making the request (for X-User-Id header).
     * @param requestUserRoles Comma-separated roles of the user making the request (for X-User-Roles header).
     * @return ResponseEntity containing the UserProfileDto.
     */
    @GetMapping("/{userId}")
    ResponseEntity<ApiResponse<UserProfileDto>> getProfileByUserId( 
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("X-User-Id") Long requestUserId,
            @RequestHeader("X-User-Roles") String requestUserRoles); 

    /**
     * Creates or updates the profile for the specified user ID.
     * Requires Authorization header. X-User-Id/Roles might be needed by profile-service if it re-uses auth logic.
     *
     * @param userId The ID of the user whose profile to save (in path).
     * @param profileRequest The profile data.
     * @param bearerToken The full Authorization header value.
     * @param requestUserId The ID of the user making the request (for X-User-Id header, optional for internal?).
     * @param requestUserRoles Comma-separated roles of the user making the request (for X-User-Roles header, optional for internal?).
     * @return ResponseEntity containing the saved UserProfileDto.
     */
    @PutMapping("/{userId}")
    ResponseEntity<ApiResponse<UserProfileDto>> saveProfile( 
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ProfileCreationRequest profileRequest,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader(name = "X-User-Id", required = false) Long requestUserId,
            @RequestHeader(name = "X-User-Roles", required = false) String requestUserRoles);
}
