package com.stdiscm.frontend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; 
import org.springframework.security.web.context.SecurityContextRepository; 

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Use Lombok for dependency injection
public class SecurityConfig {

    // Inject the custom logout success handler
    private final HttpStatusReturningLogoutSuccessHandler logoutSuccessHandler; 

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF 
            .csrf().disable() 
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED) 
            )
            .authorizeHttpRequests(authz -> authz
                // Allow access to public pages and static resources
                .antMatchers(
                    "/", 
                    "/login", 
                    "/register",
                    "/app-login", 
                    "/js/**", 
                    "/css/**", 
                    "/images/**", 
                    "/favicon.ico",
                    "/error" 
                ).permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout") // POST to /logout
                .logoutSuccessHandler(logoutSuccessHandler) // Use custom handler instead of redirect
                .invalidateHttpSession(true) 
                .deleteCookies("JSESSIONID") 
                .permitAll() 
            )
            // Explicitly configure the SecurityContextRepository
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            ); 

        return http.build();
    }

    // Define the SecurityContextRepository bean
    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Use the standard HttpSession based repository
        return new HttpSessionSecurityContextRepository();
    }

}
