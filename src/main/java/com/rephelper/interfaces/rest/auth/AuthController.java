package com.rephelper.interfaces.rest.auth;

import java.util.UUID;
import java.util.Map;

import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication")
@Slf4j
public class AuthController {

    private final FirebaseAuthAdapter firebaseAuthAdapter;
    private final UserServicePort userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/login")
    @Operation(summary = "Login with Firebase token", description = "Authenticates user with Firebase token and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Tentativa de login recebida no controlador");
            
            // Verify Firebase token
            String firebaseUid = firebaseAuthAdapter.verifyToken(loginRequest.getFirebaseToken());
            log.info("Firebase token verificado com sucesso. UID: {}", firebaseUid);
            
            // Check if user exists
            User user = userService.getUserByFirebaseUid(firebaseUid);
            log.info("Usuário encontrado: {}", user.getEmail());
            
            // Update last login
            user = userService.updateLastLogin(user.getId());
            
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user.getId());
            log.info("Token JWT gerado com sucesso");
            
            // Map user to response
            UserResponse userResponse = userDtoMapper.toUserResponse(user);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .build());
        } catch (AuthenticationException e) {
            log.error("Erro de autenticação: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .error(e.getMessage())
                            .build());
        } catch (ResourceNotFoundException e) {
            log.error("Usuário não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthResponse.builder()
                            .error("Usuário não encontrado com este token")
                            .build());
        } catch (Exception e) {
            log.error("Erro no processo de login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .error("Erro interno ao processar login")
                            .build());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            log.info("Solicitação de refresh de token recebida");
            
            // Get user ID from token without checking expiration
            UUID userId = jwtTokenProvider.getUserIdFromToken(request.getToken());
            log.info("ID do usuário extraído do token: {}", userId);
            
            // Get user
            User user = userService.getUserById(userId);
            log.info("Usuário encontrado: {}", user.getEmail());
            
            // Generate new token
            String newToken = jwtTokenProvider.generateToken(user.getId());
            log.info("Novo token JWT gerado com sucesso");
            
            // Map user to response
            UserResponse userResponse = userDtoMapper.toUserResponse(user);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(newToken)
                    .user(userResponse)
                    .build());
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .error("Token inválido ou expirado")
                            .build());
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up with Firebase token", description = "Creates a new user account using Firebase authentication")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody CreateUserRequest request) {
        try {
            log.info("Solicitação de cadastro recebida para: {}", request.getEmail());
            
            try {
                User existingUser = userService.getUserByFirebaseUid(request.getFirebaseUid());
                log.info("Usuário já existente, realizando login: {}", existingUser.getEmail());
                
                // If we get here, user exists - just log them in
                String token = jwtTokenProvider.generateToken(existingUser.getId());
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(token)
                        .user(userDtoMapper.toUserResponse(existingUser))
                        .build());
            } catch (ResourceNotFoundException e) {
                // User doesn't exist, create a new one
                log.info("Criando novo usuário: {}", request.getEmail());
                User newUser = userDtoMapper.toUser(request);
                User createdUser = userService.createUser(newUser);
                
                // Generate JWT token
                String token = jwtTokenProvider.generateToken(createdUser.getId());
                log.info("Usuário criado com sucesso: {}", createdUser.getEmail());
                
                // Return auth response with token and user
                return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                        .token(token)
                        .user(userDtoMapper.toUserResponse(createdUser))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erro no processo de cadastro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .error("Erro ao processar cadastro: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the user's current session")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("Solicitação de logout recebida");
            
            // Extrair token do cabeçalho
            String token = authHeader.substring(7); // Remover "Bearer "
            
            // Aqui poderia implementar uma blacklist de tokens, se necessário
            // Por enquanto, apenas registramos o logout
            log.info("Logout realizado com sucesso");
            
            return ResponseEntity.ok().body(Map.of("message", "Logout realizado com sucesso"));
        } catch (Exception e) {
            log.error("Erro ao processar logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao processar logout"));
        }
    }
}