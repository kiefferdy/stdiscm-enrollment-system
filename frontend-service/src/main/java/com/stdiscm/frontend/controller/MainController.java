package com.stdiscm.frontend.controller;

import com.stdiscm.common.dto.ApiResponse;
import com.stdiscm.common.dto.ProfileCreationRequest;
import com.stdiscm.common.dto.UserProfileDto;
import com.stdiscm.common.model.User;
import com.stdiscm.frontend.client.AuthServiceClient;
import com.stdiscm.frontend.client.ProfileServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import java.util.stream.Collectors;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.stdiscm.frontend.security.CustomUserDetails;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final AuthServiceClient authServiceClient;
    private final ProfileServiceClient profileServiceClient;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Redirect authenticated users away from login page
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            log.info("Authenticated user ({}) attempted to access /login, redirecting to /dashboard", authentication.getName());
            return "redirect:/dashboard";
        }
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Redirect authenticated users away from register page
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
             log.info("Authenticated user ({}) attempted to access /register, redirecting to /dashboard", authentication.getName());
            return "redirect:/dashboard";
        }
        return "register";
    }
    // Helper method to add JWT to model if authenticated
    private void addJwtToModelIfAuthenticated(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            model.addAttribute("jwtToken", principal.getJwtToken());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "dashboard";
    }
    
    @GetMapping("/courses")
    public String courses(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "courses";
    }
    
    @GetMapping("/enrollments")
    public String enrollments(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "enrollments";
    }
    
    @GetMapping("/grades")
    public String grades(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "grades";
    }
    
    @GetMapping("/faculty/courses")
    public String facultyCourses(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "faculty/courses";
    }
    
    @GetMapping("/faculty/grades")
    public String facultyGrades(Model model) {
        addJwtToModelIfAuthenticated(model);
        return "faculty/grades";
    }

    @GetMapping("/profile")
    // Try getting Authentication directly from SecurityContextHolder
    public String profile(Model model) { 

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check for valid authentication and CustomUserDetails principal
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            log.warn("Profile accessed without valid CustomUserDetails in SecurityContext. Principal type: {}", 
                     (authentication != null && authentication.getPrincipal() != null) ? authentication.getPrincipal().getClass().getName() : "null");
            model.addAttribute("error", "User not authenticated.");
            return "redirect:/login?error=session_invalid"; 
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();
        String username = principal.getUsername();
        String jwtToken = principal.getJwtToken(); 
        String bearerToken = "Bearer " + jwtToken; 
        // Extract roles and format for header
        String rolesHeader = principal.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) 
                                    .collect(Collectors.joining(","));
        log.info("Loading profile page for user via SecurityContext/CustomUserDetails: {}, ID: {}, Roles: {}", username, userId, rolesHeader);
        model.addAttribute("jwtToken", jwtToken); 

        try {
            // 1. Get full user details from auth-service
            // Pass the constructed bearerToken explicitly
            ResponseEntity<ApiResponse<User>> userResponse = authServiceClient.getCurrentUserDetails(bearerToken); 
            
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null || !userResponse.getBody().isSuccess()) {
                 log.error("Failed to fetch current user details for userId {}. Status: {}, Body: {}", userId, userResponse.getStatusCode(), userResponse.getBody());
                 model.addAttribute("error", "Could not retrieve your user information.");
                 return "profile";
            }
            User currentUser = userResponse.getBody().getData();
            // Security check: Ensure fetched user ID matches the principal's ID
             if (!currentUser.getId().equals(userId)) {
                 log.error("Mismatch between principal userId ({}) and fetched user ID ({}).", userId, currentUser.getId());
                 model.addAttribute("error", "User identity mismatch.");
                 // Optionally clear context and redirect to login?
                 SecurityContextHolder.clearContext(); 
                 return "redirect:/login?error=identity_mismatch";
             }
            model.addAttribute("currentUser", currentUser); // Add basic user info for potential display

            // 2. Try to get profile from profile-service
            try {
                // Pass all required headers explicitly
                ResponseEntity<ApiResponse<UserProfileDto>> profileResponse = profileServiceClient.getProfileByUserId(userId, bearerToken, userId, rolesHeader);
                
                if (profileResponse.getStatusCode().is2xxSuccessful() && profileResponse.getBody() != null && profileResponse.getBody().isSuccess()) {
                    log.info("Profile found for userId: {}", userId);
                    model.addAttribute("profile", profileResponse.getBody().getData());
                } else {
                     log.error("Unexpected successful response when fetching profile for userId {}. Body: {}", userId, profileResponse.getBody());
                     model.addAttribute("error", "Failed to load profile data.");
                }

            } catch (FeignException.NotFound e) {
                // 3. Profile not found (404), attempt lazy creation
                log.info("Profile not found for userId: {}. Attempting lazy creation.", userId);
                
                try {
                    // Construct request from fetched User data
                    String firstName = currentUser.getFullName();
                    String lastName = "";
                    int lastSpace = currentUser.getFullName().lastIndexOf(' ');
                    if (lastSpace > 0) {
                        firstName = currentUser.getFullName().substring(0, lastSpace);
                        lastName = currentUser.getFullName().substring(lastSpace + 1);
                    }
                    String userType = currentUser.getRoles().stream().findFirst().orElse("UNKNOWN").toUpperCase();

                    ProfileCreationRequest creationRequest = new ProfileCreationRequest(
                            userId, userType, firstName, lastName, currentUser.getEmail()
                    );

                    // Call profile-service to create/save
                    // Pass all required headers explicitly (making X-User-Id/Roles optional for now in client)
                    ResponseEntity<ApiResponse<UserProfileDto>> creationResponse = profileServiceClient.saveProfile(userId, creationRequest, bearerToken, userId, rolesHeader);

                    if (creationResponse.getStatusCode().is2xxSuccessful() && creationResponse.getBody() != null && creationResponse.getBody().isSuccess()) {
                        log.info("Successfully created profile lazily for userId: {}", userId);
                        model.addAttribute("profile", creationResponse.getBody().getData());
                        model.addAttribute("info", "Your profile was created successfully."); 
                    } else {
                        log.error("Lazy profile creation failed for userId {}. Status: {}, Body: {}", userId, creationResponse.getStatusCode(), creationResponse.getBody());
                        model.addAttribute("error", "We couldn't automatically create your profile. Please try saving it manually or contact support.");
                    }
                } catch (Exception creationEx) {
                     log.error("Exception during lazy profile creation for userId {}: {}", userId, creationEx.getMessage(), creationEx);
                     model.addAttribute("error", "An error occurred while trying to create your profile.");
                }
            } catch (Exception profileEx) {
                 log.error("Error fetching profile for userId {}: {}", userId, profileEx.getMessage(), profileEx);
                 model.addAttribute("error", "An error occurred while loading your profile.");
            }

        } catch (Exception userEx) {
            log.error("Error fetching current user details from auth-service for userId {}: {}", userId, userEx.getMessage(), userEx);
            model.addAttribute("error", "Could not retrieve your user information.");
        }

        return "profile";
    }
}
