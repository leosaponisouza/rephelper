package com.rephelper.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidade JPA para persistência de convites para eventos no banco de dados.
 */
@Entity
@Table(name = "event_invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EventInvitationJpaEntity.EventInvitationId.class)
public class EventInvitationJpaEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventJpaEntity event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventInvitationStatus status;

    /**
     * Classe ID composta para chave primária composta
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInvitationId implements Serializable {
        private static final long serialVersionUID = 1L;

        private UserJpaEntity user;
        private EventJpaEntity event;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventInvitationId that = (EventInvitationId) o;
            return Objects.equals(user, that.user) && Objects.equals(event, that.event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, event);
        }
    }

    /**
     * Status possíveis para um convite de evento
     */
    public enum EventInvitationStatus {
        INVITED, CONFIRMED, DECLINED
    }
}
