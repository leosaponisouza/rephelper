package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object que representa um convite para um evento.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitation {
    private User user;
    private Event.InvitationStatus status;

    /**
     * Confirma o convite
     */
    public void confirm() {
        this.status = Event.InvitationStatus.CONFIRMED;
    }

    /**
     * Recusa o convite
     */
    public void decline() {
        this.status = Event.InvitationStatus.DECLINED;
    }

    /**
     * Verifica se o convite foi confirmado
     */
    public boolean isConfirmed() {
        return this.status == Event.InvitationStatus.CONFIRMED;
    }

    /**
     * Verifica se o convite foi recusado
     */
    public boolean isDeclined() {
        return this.status == Event.InvitationStatus.DECLINED;
    }

    /**
     * Verifica se o convite está pendente
     */
    public boolean isPending() {
        return this.status == Event.InvitationStatus.INVITED;
    }
}