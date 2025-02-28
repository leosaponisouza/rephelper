package com.rephelper.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.rephelper.domain.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de usuário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    private String profilePictureUrl;

    @NotBlank(message = "Firebase UID is required")
    private String firebaseUid;

    @NotNull(message = "Authentication provider is required")
    private User.AuthProvider provider;

    private User.UserRole role;
}
