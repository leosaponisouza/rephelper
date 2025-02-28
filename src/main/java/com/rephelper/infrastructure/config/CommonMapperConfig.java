package com.rephelper.infrastructure.config;

import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.context.annotation.Configuration;

/**
 * Helper class to break circular dependencies between mappers
 */
@Configuration
public class CommonMapperConfig {

    // Simple mapping methods to break circular dependencies

    public User mapUserWithoutRepublic(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getUuid())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .firebaseUid(entity.getFirebaseUid())
                .provider(mapAuthProvider(entity.getProvider()))
                .role(mapUserRole(entity.getRole()))
                .status(mapUserStatus(entity.getStatus()))
                .isAdmin(entity.getIsAdmin())
                .entryDate(entity.getEntryDate())
                .departureDate(entity.getDepartureDate())
                .createdAt(entity.getCreatedAt())
                .lastLogin(entity.getLastLogin())
                .build();
    }

    public UserJpaEntity mapUserEntityWithoutRepublic(User user) {
        if (user == null) return null;

        return UserJpaEntity.builder()
                .uuid(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .firebaseUid(user.getFirebaseUid())
                .provider(mapAuthProviderToEntity(user.getProvider()))
                .role(mapUserRoleToEntity(user.getRole()))
                .status(mapUserStatusToEntity(user.getStatus()))
                .isAdmin(user.getIsAdmin())
                .entryDate(user.getEntryDate())
                .departureDate(user.getDepartureDate())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    public Republic mapRepublicWithoutUsers(RepublicJpaEntity entity) {
        if (entity == null) return null;

        return Republic.builder()
                .id(entity.getUuid())
                .name(entity.getName())
                .code(entity.getCode())
                .address(mapAddress(entity))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RepublicJpaEntity mapRepublicEntityWithoutUsers(Republic republic) {
        if (republic == null) return null;

        RepublicJpaEntity entity = new RepublicJpaEntity();
        entity.setUuid(republic.getId());
        entity.setName(republic.getName());
        entity.setCode(republic.getCode());

        if (republic.getAddress() != null) {
            entity.setStreet(republic.getAddress().getStreet());
            entity.setNumber(republic.getAddress().getNumber());
            entity.setComplement(republic.getAddress().getComplement());
            entity.setNeighborhood(republic.getAddress().getNeighborhood());
            entity.setCity(republic.getAddress().getCity());
            entity.setState(republic.getAddress().getState());
            entity.setZipCode(republic.getAddress().getZipCode());
        }

        entity.setCreatedAt(republic.getCreatedAt());
        entity.setUpdatedAt(republic.getUpdatedAt());

        return entity;
    }

    // Helper methods for mapping address
    private com.rephelper.domain.model.Address mapAddress(RepublicJpaEntity entity) {
        return com.rephelper.domain.model.Address.builder()
                .street(entity.getStreet())
                .number(entity.getNumber())
                .complement(entity.getComplement())
                .neighborhood(entity.getNeighborhood())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .build();
    }

    // Add enum mapping methods here
    private User.UserRole mapUserRole(UserJpaEntity.UserRole role) {
        if (role == null) return null;

        switch (role) {
            case ADMIN: return User.UserRole.ADMIN;
            case USER: return User.UserRole.USER;
            case RESIDENT: return User.UserRole.RESIDENT;
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private UserJpaEntity.UserRole mapUserRoleToEntity(User.UserRole role) {
        if (role == null) return null;

        switch (role) {
            case ADMIN: return UserJpaEntity.UserRole.ADMIN;
            case USER: return UserJpaEntity.UserRole.USER;
            case RESIDENT: return UserJpaEntity.UserRole.RESIDENT;
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private User.UserStatus mapUserStatus(UserJpaEntity.UserStatus status) {
        if (status == null) return null;

        switch (status) {
            case ACTIVE: return User.UserStatus.ACTIVE;
            case INACTIVE: return User.UserStatus.INACTIVE;
            case BANNED: return User.UserStatus.BANNED;
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private UserJpaEntity.UserStatus mapUserStatusToEntity(User.UserStatus status) {
        if (status == null) return null;

        switch (status) {
            case ACTIVE: return UserJpaEntity.UserStatus.ACTIVE;
            case INACTIVE: return UserJpaEntity.UserStatus.INACTIVE;
            case BANNED: return UserJpaEntity.UserStatus.BANNED;
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private User.AuthProvider mapAuthProvider(UserJpaEntity.AuthProvider provider) {
        if (provider == null) return null;

        switch (provider) {
            case EMAIL: return User.AuthProvider.EMAIL;
            case GOOGLE: return User.AuthProvider.GOOGLE;
            case FACEBOOK: return User.AuthProvider.FACEBOOK;
            case PHONE: return User.AuthProvider.PHONE;
            case GITHUB: return User.AuthProvider.GITHUB;
            case CUSTOM: return User.AuthProvider.CUSTOM;
            default: throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }

    private UserJpaEntity.AuthProvider mapAuthProviderToEntity(User.AuthProvider provider) {
        if (provider == null) return null;

        switch (provider) {
            case EMAIL: return UserJpaEntity.AuthProvider.EMAIL;
            case GOOGLE: return UserJpaEntity.AuthProvider.GOOGLE;
            case FACEBOOK: return UserJpaEntity.AuthProvider.FACEBOOK;
            case PHONE: return UserJpaEntity.AuthProvider.PHONE;
            case GITHUB: return UserJpaEntity.AuthProvider.GITHUB;
            case CUSTOM: return UserJpaEntity.AuthProvider.CUSTOM;
            default: throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
}