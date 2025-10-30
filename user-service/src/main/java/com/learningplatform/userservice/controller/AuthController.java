package com.learningplatform.userservice.controller;

import com.learningplatform.userservice.dto.LoginRequestDto;
import com.learningplatform.userservice.dto.LoginResponseDto;
import com.learningplatform.userservice.dto.UserRequestDto;
import com.learningplatform.userservice.dto.UserResponseDto;
import com.learningplatform.userservice.entity.User;
import com.learningplatform.userservice.security.JwtUtil;
import com.learningplatform.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 * 
 * POST /api/auth/register - Register new user
 * POST /api/auth/login - Login and get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto request) {
        log.info("Received registration request for email: {}", request.getEmail());
        
        // Create user with hashed password
        UserResponseDto response = userService.createUser(request);
        
        log.info("User registered successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Received login request for email: {}", request.getEmail());

        try {
            // Get user by email
            UserResponseDto user = userService.getUserByEmail(request.getEmail());
            
            // Verify password
            User userEntity = userService.getUserEntityById(user.getId());
            if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
                log.warn("Invalid password attempt for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponseDto.builder()
                                .token(null)
                                .build());
            }

            // Generate JWT token
            String username = (user.getFirstName() != null ? user.getFirstName() : "") + 
                            (user.getLastName() != null ? " " + user.getLastName() : "");
            username = username.trim().isEmpty() ? request.getEmail() : username;
            
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), username);

            LoginResponseDto response = new LoginResponseDto(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole()
            );

            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDto.builder()
                            .token(null)
                            .build());
        }
    }
}
