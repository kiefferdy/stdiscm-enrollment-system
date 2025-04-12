package com.stdiscm.frontend.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", path = "/api")
public interface AuthServiceClient {

    /**
     * Retrieves the details of the currently authenticated user.
     * Requires the Authorization header to be passed along.
     *
     * @param bearerToken The full Authorization header value (e.g., "Bearer eyJ...").
     * @return ResponseEntity containing the user details.
     */
    @GetMapping("/users/me")
    ResponseEntity<ApiResponse<User>> getCurrentUserDetails(
            @RequestHeader("Authorization") String bearerToken);
}
