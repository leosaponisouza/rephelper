package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.config.CommonMapperConfig;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private CommonMapperConfig commonMapperConfig;

    // Make sure these methods are public and correctly named
    public User toDomainEntity(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        User user = commonMapperConfig.mapUserWithoutRepublic(jpaEntity);

        if (jpaEntity.getCurrentRepublic() != null) {
            Republic currentRepublic = commonMapperConfig.mapRepublicWithoutUsers(jpaEntity.getCurrentRepublic());
            user = User.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .firebaseUid(user.getFirebaseUid())
                    .provider(user.getProvider())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .currentRepublic(currentRepublic)
                    .isAdmin(user.getIsAdmin())
                    .entryDate(user.getEntryDate())
                    .departureDate(user.getDepartureDate())
                    .createdAt(user.getCreatedAt())
                    .lastLogin(user.getLastLogin())
                    .build();
        }

        return user;
    }

    public UserJpaEntity toJpaEntity(User domainEntity) {
        if (domainEntity == null) return null;

        UserJpaEntity entity = commonMapperConfig.mapUserEntityWithoutRepublic(domainEntity);

        if (domainEntity.getCurrentRepublic() != null) {
            RepublicJpaEntity currentRepublic = commonMapperConfig.mapRepublicEntityWithoutUsers(domainEntity.getCurrentRepublic());
            entity.setCurrentRepublic(currentRepublic);
        }

        return entity;
    }

    public User toDomainEntityWithoutRepublic(UserJpaEntity jpaEntity) {
        return commonMapperConfig.mapUserWithoutRepublic(jpaEntity);
    }

    public UserJpaEntity toJpaEntityWithoutRepublic(User domainEntity) {
        return commonMapperConfig.mapUserEntityWithoutRepublic(domainEntity);
    }

    // Enum mapping methods
    public UserJpaEntity.UserRole mapToJpaUserRole(User.UserRole domainRole) {
        if (domainRole == null) return null;

        switch (domainRole) {
            case ADMIN:
                return UserJpaEntity.UserRole.ADMIN;
            case USER:
                return UserJpaEntity.UserRole.USER;
            case RESIDENT:
                return UserJpaEntity.UserRole.RESIDENT;
            default:
                throw new IllegalArgumentException("Unknown user role: " + domainRole);
        }
    }

    public User.UserRole mapToDomainUserRole(UserJpaEntity.UserRole jpaRole) {
        if (jpaRole == null) return null;

        switch (jpaRole) {
            case ADMIN:
                return User.UserRole.ADMIN;
            case USER:
                return User.UserRole.USER;
            case RESIDENT:
                return User.UserRole.RESIDENT;
            default:
                throw new IllegalArgumentException("Unknown user role: " + jpaRole);
        }
    }

    public UserJpaEntity.UserStatus mapToJpaUserStatus(User.UserStatus domainStatus) {
        if (domainStatus == null) return null;

        switch (domainStatus) {
            case ACTIVE:
                return UserJpaEntity.UserStatus.ACTIVE;
            case INACTIVE:
                return UserJpaEntity.UserStatus.INACTIVE;
            case BANNED:
                return UserJpaEntity.UserStatus.BANNED;
            default:
                throw new IllegalArgumentException("Unknown user status: " + domainStatus);
        }
    }

    public User.UserStatus mapToDomainUserStatus(UserJpaEntity.UserStatus jpaStatus) {
        if (jpaStatus == null) return null;

        switch (jpaStatus) {
            case ACTIVE:
                return User.UserStatus.ACTIVE;
            case INACTIVE:
                return User.UserStatus.INACTIVE;
            case BANNED:
                return User.UserStatus.BANNED;
            default:
                throw new IllegalArgumentException("Unknown user status: " + jpaStatus);
        }
    }

    public UserJpaEntity.AuthProvider mapToJpaAuthProvider(User.AuthProvider domainProvider) {
        if (domainProvider == null) return null;

        switch (domainProvider) {
            case EMAIL:
                return UserJpaEntity.AuthProvider.EMAIL;
            case GOOGLE:
                return UserJpaEntity.AuthProvider.GOOGLE;
            case FACEBOOK:
                return UserJpaEntity.AuthProvider.FACEBOOK;
            case PHONE:
                return UserJpaEntity.AuthProvider.PHONE;
            case GITHUB:
                return UserJpaEntity.AuthProvider.GITHUB;
            case CUSTOM:
                return UserJpaEntity.AuthProvider.CUSTOM;
            default:
                throw new IllegalArgumentException("Unknown auth provider: " + domainProvider);
        }
    }

    public User.AuthProvider mapToDomainAuthProvider(UserJpaEntity.AuthProvider jpaProvider) {
        if (jpaProvider == null) return null;

        switch (jpaProvider) {
            case EMAIL:
                return User.AuthProvider.EMAIL;
            case GOOGLE:
                return User.AuthProvider.GOOGLE;
            case FACEBOOK:
                return User.AuthProvider.FACEBOOK;
            case PHONE:
                return User.AuthProvider.PHONE;
            case GITHUB:
                return User.AuthProvider.GITHUB;
            case CUSTOM:
                return User.AuthProvider.CUSTOM;
            default:
                throw new IllegalArgumentException("Unknown auth provider: " + jpaProvider);
        }
    }
}