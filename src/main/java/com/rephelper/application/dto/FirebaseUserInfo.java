package com.rephelper.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para informações do usuário do Firebase
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUserInfo {
    private String uuid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String phoneNumber;
    private boolean emailVerified;
    private String provider;
}
