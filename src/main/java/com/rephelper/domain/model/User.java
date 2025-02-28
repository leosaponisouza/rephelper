package com.rephelper.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio que representa um usuário no sistema.
 * Esta classe contém apenas comportamentos e regras de negócio,
 * independente da persistência.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String firebaseUid;
    private AuthProvider provider;
    private UserRole role;
    private UserStatus status;
    private Republic currentRepublic;
    private Boolean isAdmin;
    private Boolean isActiveResident;
    private LocalDateTime entryDate;
    private LocalDateTime departureDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    /**
     * Provedor de autenticação usado pelo usuário
     */
    public enum AuthProvider {
        EMAIL, GOOGLE, FACEBOOK, PHONE, GITHUB, CUSTOM
    }

    /**
     * Papel do usuário no sistema
     */
    public enum UserRole {
        ADMIN, USER, RESIDENT
    }

    /**
     * Status do usuário no sistema
     */
    public enum UserStatus {
        ACTIVE, INACTIVE, BANNED
    }

    // Métodos de comportamento do domínio

    /**
     * Atualiza o último login do usuário
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Inativa o usuário
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Verifica se o usuário é administrador da república atual
     */
    public boolean isRepublicAdmin() {
        return this.currentRepublic != null && Boolean.TRUE.equals(this.isAdmin);
    }

    /**
     * Verifica se o usuário é administrador do sistema
     */
    public boolean isSystemAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * Associa o usuário a uma república
     *
     * @param republic A república a ser associada
     */
    public void joinRepublic(Republic republic) {
        this.currentRepublic = republic;

        // Define a data de entrada se for a primeira vez
        if (this.entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }

        // Limpa a data de saída e marca como ativo
        this.departureDate = null;
        this.isActiveResident = true;
    }

    /**
     * Remove o usuário da república atual
     */
    public void leaveRepublic() {
        if (this.currentRepublic != null) {
            this.departureDate = LocalDateTime.now();
            this.isActiveResident = false;
            this.isAdmin = false;
            this.currentRepublic = null;
        }
    }

    /**
     * Define o usuário como administrador da república
     */
    public void makeRepublicAdmin() {
        if (this.currentRepublic != null) {
            this.isAdmin = true;
        }
    }

    /**
     * Remove o status de administrador da república
     */
    public void removeRepublicAdmin() {
        this.isAdmin = false;
    }

    /**
     * Atualiza informações básicas do usuário
     */
    public void updateProfile(String name, String email, String phoneNumber, String profilePictureUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (email != null && !email.isBlank()) {
            this.email = email;
        }

        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }

        if (profilePictureUrl != null) {
            this.profilePictureUrl = profilePictureUrl;
        }
    }
}