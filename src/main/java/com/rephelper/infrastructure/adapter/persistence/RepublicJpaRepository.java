package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para Repúblicas
 */
@Repository
public interface RepublicJpaRepository extends JpaRepository<RepublicJpaEntity, UUID> {
    Optional<RepublicJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
    List<RepublicJpaEntity> findByOwnerUuid(UUID ownerId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.currentRepublic.uuid = :republicId")
    List<UserJpaEntity> findMembers(@Param("republicId") UUID republicId);

}
