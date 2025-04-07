package com.stdiscm.course.client;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// The name "auth-service" should match the service name registered with Eureka
@FeignClient(name = "auth-service") 
public interface AuthServiceClient {

    // Path matches the endpoint created in AuthController
    @GetMapping("/api/users/{id}") 
    ResponseEntity<ApiResponse<User>> getUserById(@PathVariable("id") Long id); 
    
    // Note: Feign typically handles the ResponseEntity and ApiResponse unwrapping,
    // but defining it this way gives more control over error handling if needed.
    // A simpler alternative could be:
    // @GetMapping("/api/users/{id}")
    // User getUserById(@PathVariable("id") Long id); 
    // However, this might throw an exception directly if the API call fails or returns an error structure.
}
