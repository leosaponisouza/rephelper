package com.rephelper.application.dto.response;

import com.rephelper.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID; /**
 * DTO para resposta de usuário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID uid;
    private String name;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private UUID currentRepublicId;
    private String currentRepublicName;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String status;
    private String firebaseUid;
    private User.AuthProvider provider;
    private LocalDateTime entryDate;
    private LocalDateTime departureDate;
}