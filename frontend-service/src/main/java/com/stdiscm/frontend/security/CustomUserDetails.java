package com.stdiscm.frontend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 2L;

    private final Long userId;
    private final String username;
    private final String jwtToken;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, String username, String jwtToken, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.jwtToken = jwtToken;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Return null or a dummy value; password check is not done here
        return null; 
    }

    @Override
    public String getUsername() {
        return username;
    }

    // --- Account status methods ---
    // Implement these based on application logic or JWT claims if available
    // For now, assume accounts are always valid after successful JWT validation

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or check JWT expiry if needed again
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implement locking logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // JWT itself is the credential; already validated
    }

    @Override
    public boolean isEnabled() {
        return true; // Implement enabling/disabling logic if needed
    }
}
