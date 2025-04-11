package com.stdiscm.frontend.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A LogoutSuccessHandler that simply returns an HTTP status code (e.g., 200 OK)
 * instead of performing a redirect. The client-side JavaScript will handle the redirect.
 */
@Component // Register as a bean
public class HttpStatusReturningLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException, ServletException {
        
        // Optionally log logout success
        if (authentication != null && authentication.getName() != null) {
            System.out.println("User logged out successfully: " + authentication.getName());
        } else {
             System.out.println("Logout successful for anonymous or unknown user.");
        }

        response.setStatus(HttpStatus.OK.value());
        // Do not perform a redirect here
    }
}
