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
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User createdUser = authService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok(ApiResponse.success("Token is valid"));
    }
}
