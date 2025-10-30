package com.learningplatform.gateway.filter;

import com.learningplatform.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * Global JWT authentication filter for API Gateway.
 * 
 * Flow:
 * 1. Check if endpoint requires authentication
 * 2. Extract JWT from Authorization header
 * 3. Validate JWT token
 * 4. Add X-User-Id and X-Username headers for downstream services
 * 5. Forward request to backend service
 * 
 * Public endpoints (no auth required):
 * - /api/auth/** (login, register)
 * - /api/courses (GET - browse courses)
 * - /api/discussions (GET - read discussions)
 * - /api/analytics (GET - read analytics)
 * - /eureka/** (discovery service UI)
 * - /actuator/** (health checks)
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check if endpoint is public (no authentication required)
        if (isPublicEndpoint(request.getPath().value(), request.getMethod().toString())) {
            return chain.filter(exchange);
        }

        // Extract Authorization header
        List<String> authHeaders = request.getHeaders().get("Authorization");
        
        if (authHeaders == null || authHeaders.isEmpty()) {
            return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = authHeaders.get(0);
        
        if (!authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // Validate JWT and extract claims
            Claims claims = jwtUtil.validateToken(token);
            
            if (claims == null) {
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information
            String userId = jwtUtil.extractUserId(claims);
            String username = jwtUtil.extractUsername(claims);

            // Add user info to request headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if endpoint is public (no authentication required)
     */
    private boolean isPublicEndpoint(String path, String method) {
        // Public endpoints - no auth required
        if (path.startsWith("/api/auth/")) return true;  // Login, register
        if (path.startsWith("/eureka")) return true;     // Eureka dashboard
        if (path.startsWith("/actuator")) return true;   // Health checks
        
        // GET requests to these endpoints are public (browse without login)
        if ("GET".equals(method)) {
            if (path.startsWith("/api/courses")) return true;       // Browse courses
            if (path.startsWith("/api/discussions")) return true;   // Read discussions
            if (path.startsWith("/api/analytics")) return true;     // View analytics
            if (path.startsWith("/api/reviews")) return true;       // Read reviews
        }
        
        // All other endpoints require authentication
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                                   status.getReasonPhrase(), message);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // High priority - run before other filters
    }
}
