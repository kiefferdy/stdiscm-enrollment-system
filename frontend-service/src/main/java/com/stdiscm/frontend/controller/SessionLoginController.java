package com.stdiscm.frontend.controller;

import com.stdiscm.frontend.security.CustomUserDetails; 
import com.stdiscm.frontend.service.JwtUtilService; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Collection;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SessionLoginController {

    // Inject JwtUtilService
    private final JwtUtilService jwtUtilService; 

    // Endpoint called by client-side JS after getting JWT from auth-service
    @PostMapping("/app-login")
    public ResponseEntity<?> establishSession(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = payload.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Token is missing"));
        }

        try {
            // 1. Validate the token directly using JwtUtilService
            if (!jwtUtilService.validateToken(token)) {
                 log.warn("Attempt to establish session with invalid/expired token.");
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid or expired token"));
            }

            // 2. Extract details from the valid token
            String username = jwtUtilService.extractUsername(token);
            Long userId = jwtUtilService.extractUserId(token);
            Collection<? extends GrantedAuthority> authorities = jwtUtilService.extractAuthorities(token);
            // Removed logging

            // 3. Create CustomUserDetails principal
            CustomUserDetails userDetails = new CustomUserDetails(userId, username, token, authorities);

            // 4. Create standard authenticated token
            UsernamePasswordAuthenticationToken authenticatedToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            // 5. Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticatedToken);

            // 6. Ensure session exists
            request.getSession(true); 
            log.info("Session established successfully for user: {}. SecurityContext updated.", username);

            // 7. Return success
            return ResponseEntity.ok(Map.of("success", true, "message", "Session established"));

        } catch (Exception e) {
            log.error("Failed to establish session via JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext(); // Ensure context is cleared on failure
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(Map.of("success", false, "message", "Invalid or expired token"));
        }
    }
}
