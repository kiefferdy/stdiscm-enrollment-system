package com.stdiscm.course.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignClientInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Define a RequestInterceptor bean that will be automatically applied to Feign clients
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Get the current request attributes
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    // Get the Authorization header from the incoming request
                    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        // Add the Authorization header to the outgoing Feign request
                        template.header(AUTHORIZATION_HEADER, authorizationHeader);
                    }
                }
            }
        };
    }
}
