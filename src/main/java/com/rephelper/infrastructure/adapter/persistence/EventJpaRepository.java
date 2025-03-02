package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para Eventos
 */
@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
    List<EventJpaEntity> findByRepublicUuid(UUID republicId);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.republic.uuid = :republicId AND e.startDate > :now ORDER BY e.startDate ASC")
    List<EventJpaEntity> findUpcomingByRepublicId(@Param("republicId") UUID republicId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM EventJpaEntity e JOIN e.invitations i WHERE i.user.uuid = :userId")
    List<EventJpaEntity> findByInvitedUserId(@Param("userId") UUID userId);

    @Query("SELECT e FROM EventJpaEntity e JOIN e.invitations i WHERE i.user.uuid = :userId AND i.status = 'CONFIRMED'")
    List<EventJpaEntity> findByConfirmedUserId(@Param("userId") UUID userId);
}