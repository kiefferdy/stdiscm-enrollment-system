package com.stdiscm.auth.controller;

import com.stdiscm.auth.service.AuthService;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.JwtResponse;
import com.stdiscm.common.dto.LoginRequest;
import com.stdiscm.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api") 
public class AuthController {
    
    @Autowired
    private AuthService authService;

    // --- Authentication Endpoints ---
    
    @PostMapping("/auth/login") 
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/auth/register") 
    public ResponseEntity<?> registerUser(@Valid @RequestBody User registrationRequest) {
        try {
            // Ensure roles from the request are explicitly set on the User object
            User userToCreate = new User();
            userToCreate.setUsername(registrationRequest.getUsername());
            userToCreate.setPassword(registrationRequest.getPassword()); // Service layer will encode
            userToCreate.setEmail(registrationRequest.getEmail());
            userToCreate.setFullName(registrationRequest.getFullName());
            // Explicitly copy roles - ensures the Set is initialized and populated
            if (registrationRequest.getRoles() != null) {
                 userToCreate.setRoles(Set.copyOf(registrationRequest.getRoles())); 
            } // else roles will be an empty set by default from User model

            User createdUser = authService.createUser(userToCreate);
            // Return the created user details (excluding password)
            createdUser.setPassword(null); 
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/auth/validate") 
    public ResponseEntity<?> validateToken() {
        // This endpoint might need more logic depending on how validation is intended
        return ResponseEntity.ok(ApiResponse.success("Token is valid")); 
    }

    // --- User Management Endpoints ---

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // This case might happen if the JWT filter failed validation silently
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated (token invalid or missing)"));
        }

        Object principal = authentication.getPrincipal();
        String username;

        // Principal should be UserDetails loaded by JwtAuthFilter
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
             // Log unexpected principal type
             System.err.println("Unexpected principal type in /users/me: " + (principal != null ? principal.getClass().getName() : "null"));
             return ResponseEntity.status(500).body(ApiResponse.error("Could not determine username from principal"));
        }

        try {
            User user = authService.findUserByUsername(username); 
            user.setPassword(null); // Avoid sending password hash back
            return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", user));
        } catch (Exception e) {
            // Handle cases where user might exist in security context but not DB (unlikely but possible)
             System.err.println("Error finding user by username '" + username + "' in /users/me: " + e.getMessage());
             return ResponseEntity.status(404).body(ApiResponse.error("User details not found: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = authService.findUserById(id);
            user.setPassword(null); 
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}
