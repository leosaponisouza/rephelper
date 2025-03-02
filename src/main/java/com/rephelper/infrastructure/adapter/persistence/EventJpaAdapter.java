package com.rephelper.infrastructure.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rephelper.domain.model.Event;
import com.rephelper.domain.port.out.EventRepositoryPort;
import com.rephelper.infrastructure.entity.EventJpaEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do adaptador para o repositório de eventos usando JPA.
 */
@Component
@RequiredArgsConstructor
public class EventJpaAdapter implements EventRepositoryPort {

    private final EventJpaRepository eventJpaRepository;
    private final EventMapper eventMapper;

    @Override
    public Event save(Event event) {
        EventJpaEntity eventEntity = eventMapper.toJpaEntity(event);
        EventJpaEntity savedEntity = eventJpaRepository.save(eventEntity);
        return eventMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventJpaRepository.findById(id)
                .map(eventMapper::toDomainEntity);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll().stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByRepublicId(UUID republicId) {
        return eventJpaRepository.findByRepublicUuid(republicId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findUpcomingByRepublicId(UUID republicId) {
        return eventJpaRepository.findUpcomingByRepublicId(republicId, LocalDateTime.now()).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByInvitedUserId(UUID userId) {
        return eventJpaRepository.findByInvitedUserId(userId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByConfirmedUserId(UUID userId) {
        return eventJpaRepository.findByConfirmedUserId(userId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Event event) {
        eventJpaRepository.deleteById(event.getId());
    }
}