package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.EventInvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para Convites de Eventos
 */
@Repository
public interface EventInvitationJpaRepository extends JpaRepository<EventInvitationJpaEntity, EventInvitationJpaEntity.EventInvitationId> {
}
