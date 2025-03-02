package com.rephelper.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rephelper.domain.model.Event;

/**
 * Porta de saída para operações de repositório relacionadas a eventos.
 * Define o contrato que a camada de infraestrutura deve implementar.
 */
public interface EventRepositoryPort {
    /**
     * Salva ou atualiza um evento
     */
    Event save(Event event);

    /**
     * Busca um evento pelo ID
     */
    Optional<Event> findById(Long id);

    /**
     * Busca todos os eventos
     */
    List<Event> findAll();

    /**
     * Busca eventos por república
     */
    List<Event> findByRepublicId(UUID republicId);

    /**
     * Busca eventos futuros por república
     */
    List<Event> findUpcomingByRepublicId(UUID republicId);

    /**
     * Busca eventos que um usuário foi convidado
     */
    List<Event> findByInvitedUserId(UUID userId);

    /**
     * Busca eventos que um usuário confirmou presença
     */
    List<Event> findByConfirmedUserId(UUID userId);

    /**
     * Remove um evento
     */
    void delete(Event event);
}