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
import java.util.List;
import java.util.function.Predicate;

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
                    validateToken(token);
                    
                    // You could extract user roles here and decide whether to forward based on those
                    
                } catch (Exception e) {
                    return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }
            
            return chain.filter(exchange);
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
