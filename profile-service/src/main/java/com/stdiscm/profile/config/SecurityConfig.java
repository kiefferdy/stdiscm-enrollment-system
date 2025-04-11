package com.stdiscm.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection since we are likely using JWT/headers and not browser sessions directly
            .csrf(csrf -> csrf.disable())
            // Make the session stateless - relying on headers from gateway
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Permit all requests coming into this service
            // Authorization logic is handled within the controller based on gateway headers
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow all requests to pass security filter chain
            );

        return http.build();
    }
}
