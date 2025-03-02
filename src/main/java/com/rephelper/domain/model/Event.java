package com.rephelper.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio que representa um evento na república.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Republic republic;
    private User creator;
    @Builder.Default
    private Set<EventInvitation> invitations = new HashSet<>();
    private LocalDateTime createdAt;

    /**
     * Estado possível de um convite para evento
     */
    public enum InvitationStatus {
        INVITED, CONFIRMED, DECLINED
    }

    /**
     * Atualiza informações básicas do evento
     */
    public void updateDetails(String title, String description, LocalDateTime startDate,
                              LocalDateTime endDate, String location) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }

        if (description != null) {
            this.description = description;
        }

        if (startDate != null) {
            this.startDate = startDate;
        }

        if (endDate != null) {
            this.endDate = endDate;
        }

        if (location != null) {
            this.location = location;
        }
    }

    /**
     * Verifica se o evento já ocorreu
     */
    public boolean hasFinished() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    /**
     * Verifica se o evento está ocorrendo no momento
     */
    public boolean isHappening() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.startDate) && now.isBefore(this.endDate);
    }

    /**
     * Adiciona um convite para o evento
     */
    public void addInvitation(EventInvitation invitation) {
        this.invitations.add(invitation);
    }

    /**
     * Remove um convite do evento
     */
    public void removeInvitation(EventInvitation invitation) {
        this.invitations.removeIf(i -> i.getUser().getId().equals(invitation.getUser().getId()));
    }

    /**
     * Verifica se um usuário foi convidado para o evento
     */
    public boolean isUserInvited(UUID userId) {
        return this.invitations.stream()
                .anyMatch(i -> i.getUser().getId().equals(userId));
    }

    /**
     * Obtém o status de convite de um usuário
     */
    public InvitationStatus getUserInvitationStatus(UUID userId) {
        return this.invitations.stream()
                .filter(i -> i.getUser().getId().equals(userId))
                .findFirst()
                .map(EventInvitation::getStatus)
                .orElse(null);
    }
}