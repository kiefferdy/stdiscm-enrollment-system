package com.stdiscm.auth.controller;

import com.stdiscm.auth.service.AuthService;
import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.JwtResponse;
import com.stdiscm.common.dto.LoginRequest;
import com.stdiscm.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
// Changed base path to /api/users for clarity, keeping /api/auth for auth operations
@RequestMapping("/api") 
public class AuthController {
    
    @Autowired
    private AuthService authService;

    // --- Authentication Endpoints ---
    
    @PostMapping("/auth/login") // Updated path
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/auth/register") // Updated path
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User createdUser = authService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/auth/validate") // Updated path
    public ResponseEntity<?> validateToken() {
        // This endpoint might need more logic depending on how validation is intended
        return ResponseEntity.ok(ApiResponse.success("Token is valid")); 
    }

    // --- User Management Endpoints ---

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = authService.findUserById(id);
            // Avoid sending password hash back
            user.setPassword(null); 
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            // Assuming ResourceNotFoundException is thrown by the service
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // TODO: Add endpoints for updating user profile, listing users (admin), etc. if needed
}
