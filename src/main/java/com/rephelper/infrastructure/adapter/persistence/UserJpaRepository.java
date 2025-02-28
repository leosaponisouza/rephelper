package com.rephelper.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rephelper.infrastructure.entity.UserJpaEntity;

/**
 * Repositório JPA para Usuários
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    @Query("SELECT u FROM UserJpaEntity u WHERE u.firebaseUid = :firebaseUid")
    Optional<UserJpaEntity> findByFirebaseUid(@Param("firebaseUid") String firebaseUid);
    boolean existsByEmail(String email);
    boolean existsByFirebaseUid(String firebaseUid);
    List<UserJpaEntity> findByCurrentRepublicUuid(UUID republicId);
}

