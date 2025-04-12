package com.stdiscm.gateway.config;

import com.stdiscm.gateway.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;

@Configuration
public class GatewayConfig {
    
    @Autowired
    @Lazy
    private AuthFilter authFilter;
    
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(
                                authFilter.apply(createConfig("/api/auth/login", "/api/auth/register"))
                        ))
                        .uri("lb://auth-service"))
                .route("course-service", r -> r.path("/api/courses/**")
                        .filters(f -> f.filter(
                                authFilter.apply(createConfig("/api/courses/public"))
                        ))
                        .uri("lb://course-service"))
                .route("grade-service", r -> r.path("/api/grades/**")
                        // Use createConfig() to ensure excludedUrls list is initialized
                        .filters(f -> f.filter(authFilter.apply(createConfig())))
                        .uri("lb://grade-service"))
                .route("profile-service", r -> r.path("/api/profiles/**") 
                         .filters(f -> f.filter(authFilter.apply(createConfig())))
                         .uri("lb://profile-service")) 
                 // Frontend route - Auth handled by frontend service itself using sessions/security context
                 .route("frontend-service", r -> r.path("/**")
                         .uri("lb://frontend-service"))
                 .build();
     }
    
    private AuthFilter.Config createConfig(String... excludedUrls) {
        AuthFilter.Config config = new AuthFilter.Config();
        config.setExcludedUrls(Arrays.asList(excludedUrls));
        return config;
    }
}
