package com.stdiscm.auth.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.ProfileCreationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "profile-service")
public interface ProfileServiceClient {

    /**
     * Calls the profile-service to create/update a profile.
     * Corresponds to the saveProfile method in ProfileController.
     * Note: Feign requires @PathVariable name to match the placeholder in the path.
     * Note: We expect the profile-service to handle authorization based on headers,
     *       so we don't explicitly pass auth headers here unless a Feign interceptor is configured.
     *       (Currently, no interceptor seems configured in course-service, so assuming none needed here yet).
     *
     * @param userId The ID of the user whose profile to save.
     * @param profileRequest The profile data.
     * @return A ResponseEntity containing the ApiResponse from profile-service.
     *         We don't strictly need the response body here, but it's good practice.
     */
    @PutMapping("/api/profiles/{userId}")
    ResponseEntity<ApiResponse<Object>> saveProfile(
            @PathVariable("userId") Long userId,
            @RequestBody ProfileCreationRequest profileRequest);

}
