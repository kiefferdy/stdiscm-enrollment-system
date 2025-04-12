package com.stdiscm.frontend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtUtilService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) throws SignatureException {
         // Added try-catch specifically for SignatureException which indicates tampering/invalid key
         try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
         } catch (SignatureException e) {
             log.error("Invalid JWT signature: {}", e.getMessage());
             throw e; // Re-throw to be handled by caller
         } catch (Exception e) {
             log.error("Error parsing JWT claims: {}", e.getMessage());
             throw new RuntimeException("Invalid token", e); // Wrap other exceptions
         }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userIdClaim = extractAllClaims(token).get("userId");
        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        log.error("Invalid userId claim type in token: {}", userIdClaim != null ? userIdClaim.getClass() : "null");
        throw new RuntimeException("Invalid userId claim in token");
    }

    public List<String> extractRoles(String token) {
        Object rolesClaim = extractAllClaims(token).get("roles");
        if (rolesClaim instanceof List) {
             try {
                 // Ensure all elements are Strings
                 return ((List<?>) rolesClaim).stream()
                                             .map(obj -> (String) obj)
                                             .collect(Collectors.toList());
             } catch (ClassCastException e) {
                  log.error("Invalid roles claim content (non-String elements) in token", e);
                  throw new RuntimeException("Invalid roles claim content in token");
             }
        }
         log.error("Invalid roles claim type in token: {}", rolesClaim != null ? rolesClaim.getClass() : "null");
         throw new RuntimeException("Invalid roles claim type in token");
    }
    
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        // Roles extracted from the token already have the "ROLE_" prefix added by auth-service
        List<String> rolesWithPrefix = extractRoles(token); 
        // Create authorities directly from the extracted role strings
        return rolesWithPrefix.stream()
                .map(SimpleGrantedAuthority::new) // Assumes roles like "ROLE_FACULTY" are in the list
                .collect(Collectors.toList());
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            // If claims can't be extracted, treat as invalid/expired
            log.warn("Could not determine token expiration, treating as expired: {}", e.getMessage());
            return true; 
        }
    }

    public boolean validateToken(String token) {
        try {
            // Check expiration and signature
            if (isTokenExpired(token)) {
                return false;
            }
            // Parsing claims implicitly validates the signature
            extractAllClaims(token); 
            return true;
        } catch (SignatureException e) {
            log.error("JWT validation failed (signature): {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT validation failed (other): {}", e.getMessage());
            return false;
        }
    }
}
