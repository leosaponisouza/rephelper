package com.rephelper.infrastructure.adapter.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.entity.UserJpaEntity;

/**
 * Mapper para converter entre User do domínio e UserJpaEntity
 */
@Mapper(componentModel = "spring", uses = {RepublicMapper.class})
public abstract class UserMapper {

    @Autowired
    protected RepublicMapper republicMapper;

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "currentRepublic", source = "currentRepublic", qualifiedByName = "toEntityWithoutUsers")
    public abstract User toDomainEntity(UserJpaEntity jpaEntity);

    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "currentRepublic", source = "currentRepublic", qualifiedByName = "toJpaEntityWithoutUsers")
    public abstract UserJpaEntity toJpaEntity(User domainEntity);

    @Named("userWithoutRepublic")
    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "currentRepublic", ignore = true)
    public abstract User toDomainEntityWithoutRepublic(UserJpaEntity jpaEntity);

    @Named("userJpaWithoutRepublic")
    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "currentRepublic", ignore = true)
    public abstract UserJpaEntity toJpaEntityWithoutRepublic(User domainEntity);

    // Método para mapear enums
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

