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
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() 
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
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true) 
                .deleteCookies("JSESSIONID") 
                .permitAll() 
            )
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            ); 

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
