package com.rephelper.interfaces.rest.auth;

import java.util.UUID;

import com.rephelper.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.request.CreateUserRequest;
import com.rephelper.application.dto.request.LoginRequest;
import com.rephelper.application.dto.request.TokenRefreshRequest;
import com.rephelper.application.dto.response.AuthResponse;
import com.rephelper.application.dto.response.UserResponse;
import com.rephelper.application.mapper.UserDtoMapper;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.firebase.FirebaseAuthAdapter;
import com.rephelper.infrastructure.adapter.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication")
public class AuthController {

    private final FirebaseAuthAdapter firebaseAuthAdapter;
    private final UserServicePort userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/login")
    @Operation(summary = "Login with Firebase token", description = "Authenticates user with Firebase token and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Verify Firebase token
        String firebaseUid = firebaseAuthAdapter.verifyToken(loginRequest.getFirebaseToken());

        try {
            // Check if user exists
            User user = userService.getUserByFirebaseUid(firebaseUid);

            // Update last login
            user = userService.updateLastLogin(user.getId());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId());

            // Map user to response
            UserResponse userResponse = userDtoMapper.toUserResponse(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .build());

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        // Check if token is valid (not checking expiration)
        if (!jwtTokenProvider.validateToken(request.getToken())) {
            return ResponseEntity.badRequest().build();
        }

        // Get user ID from token
        UUID userId = jwtTokenProvider.getUserIdFromToken(request.getToken());

        // Get user
        User user = userService.getUserById(userId);

        // Generate new token
        String newToken = jwtTokenProvider.generateToken(user.getId());

        // Map user to response
        UserResponse userResponse = userDtoMapper.toUserResponse(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(newToken)
                .user(userResponse)
                .build());
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up with Firebase token", description = "Creates a new user account using Firebase authentication")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody CreateUserRequest request) {

        try {
            User existingUser = userService.getUserByFirebaseUid(request.getFirebaseUid());
            // If we get here, user exists - just log them in
            String token = jwtTokenProvider.generateToken(existingUser.getId());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(userDtoMapper.toUserResponse(existingUser))
                    .build());
        } catch (ResourceNotFoundException e) {
            // User doesn't exist, create a new one
            User newUser = userDtoMapper.toUser(request);
            User createdUser = userService.createUser(newUser);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(createdUser.getId());

            // Return auth response with token and user
            return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                    .token(token)
                    .user(userDtoMapper.toUserResponse(createdUser))
                    .build());
        }
    }
}