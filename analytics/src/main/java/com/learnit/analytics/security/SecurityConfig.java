package com.learnit.analytics.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for Analytics Service.
 * 
 * Endpoints:
 * - POST /api/analytics/ingest/** → Requires API_KEY (service-to-service)
 * - GET /api/analytics/** → Public (read analytics)
 * - Actuator endpoints → Public (for monitoring)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${analytics.security.api-key:}")
    private String apiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (read analytics)
                .requestMatchers(HttpMethod.GET, "/api/analytics/**").permitAll()
                
                // Actuator endpoints (health checks, metrics)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Ingest endpoints require API key
                .requestMatchers(HttpMethod.POST, "/api/analytics/ingest/**").authenticated()
                
                // Default deny
                .anyRequest().authenticated()
            )
            .addFilterBefore(new ApiKeyAuthenticationFilter(apiKey), 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
