package com.rephelper.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rephelper.infrastructure.entity.UserJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity.UserRole;

/**
 * Repositório JPA para Usuários
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByFirebaseUid(String firebaseUid);
    List<UserJpaEntity> findByRole(UserRole role);
    List<UserJpaEntity> findByCurrentRepublicId(UUID republicId);
    List<UserJpaEntity> findByCurrentRepublicIdAndIsActiveResident(UUID republicId, Boolean isActiveResident);
}

