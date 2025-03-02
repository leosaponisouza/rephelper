package com.rephelper.domain.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.rephelper.domain.model.Event;

/**
 * Porta de entrada definindo os casos de uso relacionados a eventos.
 * Esta interface será implementada pelo serviço de domínio.
 */
public interface EventServicePort {
    /**
     * Cria um novo evento
     */
    Event createEvent(Event event, UUID creatorUserId);

    /**
     * Obtém todos os eventos de uma república
     */
    List<Event> getAllEventsByRepublicId(UUID republicId);

    /**
     * Obtém um evento pelo ID
     */
    Event getEventById(Long id);

    /**
     * Obtém eventos futuros de uma república
     */
    List<Event> getUpcomingEventsByRepublicId(UUID republicId);

    /**
     * Atualiza um evento existente
     */
    Event updateEvent(Long id, String title, String description,
                      LocalDateTime startDate, LocalDateTime endDate,
                      String location, UUID modifierUserId);

    /**
     * Remove um evento
     */
    void deleteEvent(Long id, UUID userId);

    /**
     * Adiciona convites para um evento
     */
    Event inviteUsers(Long eventId, List<UUID> userIds, UUID inviterUserId);

    /**
     * Atualiza o status de um convite
     */
    Event updateInvitationStatus(Long eventId, UUID userId, Event.InvitationStatus status);

    /**
     * Obtém os eventos que um usuário foi convidado
     */
    List<Event> getEventsByInvitedUser(UUID userId);

    /**
     * Obtém os eventos que um usuário confirmou presença
     */
    List<Event> getEventsByConfirmedUser(UUID userId);
}