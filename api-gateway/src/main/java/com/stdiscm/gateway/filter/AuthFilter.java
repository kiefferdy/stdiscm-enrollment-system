package com.stdiscm.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            final List<String> apiEndpoints = config.getExcludedUrls();
            
            Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                    .noneMatch(uri -> r.getURI().getPath().contains(uri));
            
            if (isApiSecured.test(request)) {
                if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                    return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getOrEmpty(AUTHORIZATION_HEADER).get(0);
                if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                    return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(BEARER_PREFIX.length());
                
                try {
                    Claims claims = validateToken(token);
                    Long userId = null;
                    List<String> roles = null;

                    // Separate try-catch for robust claim extraction
                    try {
                        // Extract numeric userId from the "userId" claim
                        Object userIdClaim = claims.get("userId");
                        if (userIdClaim instanceof Number) {
                            userId = ((Number) userIdClaim).longValue();
                        } else {
                            // Log error: logger.error("Invalid userId claim type: {}", userIdClaim != null ? userIdClaim.getClass() : "null");
                            return onError(exchange, "Invalid JWT token: Invalid userId claim type", HttpStatus.BAD_REQUEST);
                        }

                        // Extract roles from the "roles" claim safely
                        Object rolesClaim = claims.get("roles");
                        if (rolesClaim instanceof List) {
                            List<?> rawRoles = (List<?>) rolesClaim;
                            // Use stream to safely map and collect Strings, handling potential type errors
                            try {
                                roles = rawRoles.stream()
                                                .map(obj -> (String) obj) // Cast each element
                                                .collect(Collectors.toList());
                            } catch (ClassCastException cce) {
                                // Log error: logger.error("Invalid roles claim content: Contains non-String elements", cce);
                                return onError(exchange, "Invalid JWT token: Invalid roles claim content", HttpStatus.BAD_REQUEST);
                            }
                        } else if (rolesClaim != null) {
                             // Log error: logger.error("Invalid roles claim type: {}", rolesClaim.getClass());
                             return onError(exchange, "Invalid JWT token: Invalid roles claim type", HttpStatus.BAD_REQUEST);
                        }

                        // Default to empty list if roles claim is missing or invalid type handled above
                        if (roles == null) {
                            roles = new ArrayList<>();
                        }

                    } catch (Exception claimEx) {
                         // Log specific claim extraction error
                         // logger.error("JWT Claim Extraction Error: {}", claimEx.getMessage(), claimEx);
                         return onError(exchange, "Invalid JWT token: Malformed claims", HttpStatus.BAD_REQUEST);
                    }

                    // Add user info as headers to the downstream request
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(userId)) // Convert Long userId to String for header
                            .header("X-User-Roles", String.join(",", roles)) // Join roles into a comma-separated string
                            .build();
                    
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    
                    return chain.filter(mutatedExchange); // Pass the mutated exchange down the chain

                } catch (Exception e) {
                    // Log the error for debugging purposes
                    // logger.error("JWT Validation Error: {}", e.getMessage()); 
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED); // More generic message
                }
            } else {
                // If the request is for an excluded URL, just pass it through
                return chain.filter(exchange);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
    
    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
        private List<String> excludedUrls;

        public List<String> getExcludedUrls() {
            return excludedUrls;
        }

        public void setExcludedUrls(List<String> excludedUrls) {
            this.excludedUrls = excludedUrls;
        }
    }
}
