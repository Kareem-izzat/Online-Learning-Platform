package com.learnit.analytics.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * API Key authentication filter for service-to-service communication.
 * 
 * Checks for X-API-Key header and validates against configured API key.
 * Used by Discussion Service to send events to Analytics ingest endpoint.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final String apiKey;

    public ApiKeyAuthenticationFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestApiKey = request.getHeader(API_KEY_HEADER);
        
        // If API key is provided and matches, authenticate
        if (requestApiKey != null && !apiKey.isEmpty() && requestApiKey.equals(apiKey)) {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    "service-account", 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to POST /api/analytics/ingest/** endpoints
        String path = request.getRequestURI();
        String method = request.getMethod();
        return !(method.equals("POST") && path.startsWith("/api/analytics/ingest"));
    }
}
