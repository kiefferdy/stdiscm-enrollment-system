package com.stdiscm.auth.service;

import com.stdiscm.auth.repository.UserRepository;
import com.stdiscm.common.dto.JwtResponse;
import com.stdiscm.auth.client.ProfileServiceClient;
import com.stdiscm.common.dto.LoginRequest;
import com.stdiscm.common.dto.ProfileCreationRequest;
import com.stdiscm.common.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.stdiscm.common.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;
    
    @Autowired 
    private JwtService jwtService;

    @Autowired(required = false)
    private ProfileServiceClient profileServiceClient;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Fetch the full User object to get ID and roles
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication: " + userDetails.getUsername()));

        // Generate token including the user ID claim
        String jwt = jwtService.generateTokenWithUserId(userDetails, user.getId()); 
        
        List<String> roles = user.getRoles().stream()
                .collect(Collectors.toList());
        
        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // --- Call Profile Service ---
        if (profileServiceClient != null) {
            try {
                // Extract first/last name from fullName (simple split based on last space)
                String firstName = savedUser.getFullName();
                String lastName = "";
                int lastSpace = savedUser.getFullName().lastIndexOf(' ');
                if (lastSpace > 0) {
                    firstName = savedUser.getFullName().substring(0, lastSpace);
                    lastName = savedUser.getFullName().substring(lastSpace + 1);
                }

                // Assuming the first role is the primary user type
                String userType = savedUser.getRoles().stream().findFirst().orElse("UNKNOWN").toUpperCase();

                ProfileCreationRequest profileRequest = new ProfileCreationRequest(
                        savedUser.getId(),
                        userType,
                        firstName,
                        lastName,
                        savedUser.getEmail()
                );

                log.info("Attempting to create profile for userId: {}", savedUser.getId());
                ResponseEntity<?> profileResponse = profileServiceClient.saveProfile(savedUser.getId(), profileRequest);
                log.info("Profile service call for userId {} completed with status: {}", savedUser.getId(), profileResponse.getStatusCode());

                // Optional: Check profileResponse status and log/handle errors more robustly
                if (!profileResponse.getStatusCode().is2xxSuccessful()) {
                     log.error("Failed to create profile for userId {}. Status: {}, Body: {}",
                             savedUser.getId(), profileResponse.getStatusCode(), profileResponse.getBody());
                     // TODO: Consider if registration should fail if profile creation fails (e.g., throw exception)
                }

            } catch (Exception e) {
                // Log Feign exception but allow registration to succeed
                log.error("Error calling profile service during registration for userId {}: {}", savedUser.getId(), e.getMessage());
            }
        } else {
            log.warn("ProfileServiceClient is not available. Skipping profile creation during registration for userId {}.", savedUser.getId());
        }
        // --- End Profile Service Call ---

        return savedUser;
    }

    // New method to find user by ID
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
